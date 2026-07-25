package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CartItem
import com.example.data.PharmacyRepository
import com.example.data.entity.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PharmacyViewModel(private val repository: PharmacyRepository) : ViewModel() {

    // Roles & Settings
    val currentEmployee = MutableStateFlow("এডমিন") // "এডমিন" or "কর্মচারী"
    val shopName = MutableStateFlow("বিসমিল্লাহ্ ফার্মেসি ও ড্রাগস")
    val shopAddress = MutableStateFlow("স্টেশন রোড, ঢাকা")
    val shopPhone = MutableStateFlow("01700000000")

    // Search queries
    val customerQuery = MutableStateFlow("")
    val medicineQuery = MutableStateFlow("")

    // Data Flows
    val allCustomers: StateFlow<List<CustomerEntity>> = customerQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.allCustomers
            else repository.searchCustomers(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalDues: StateFlow<Double> = repository.totalDuesFlow
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val allMedicines: StateFlow<List<MedicineEntity>> = medicineQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.allMedicines
            else repository.searchMedicines(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockMedicines: StateFlow<List<MedicineEntity>> = repository.lowStockMedicines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvoices: StateFlow<List<SaleInvoiceEntity>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestCashBox: StateFlow<CashBoxEntity?> = repository.latestCashBox
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val cashTransactions: StateFlow<List<CashTransactionEntity>> = repository.allCashTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart state for POS
    val cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val discountInput = MutableStateFlow("0")
    val paidInput = MutableStateFlow("0")
    val paymentMethodInput = MutableStateFlow("CASH") // CASH, BKASH, NAGAD, BANK, DUE

    init {
        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
        }
    }

    // Customer Actions
    fun getTransactionsForCustomer(customerId: Long): Flow<List<CustomerTransactionEntity>> = repository.getTransactionsForCustomer(customerId)

    fun addCustomer(name: String, phone: String, address: String) {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            repository.addCustomer(CustomerEntity(name = name, phone = phone, address = address))
        }
    }

    fun recordCustomerPayment(customerId: Long, amount: Double, paymentMethod: String, note: String) {
        viewModelScope.launch {
            repository.recordCustomerPayment(customerId, amount, paymentMethod, note)
        }
    }

    fun addCustomerDue(customerId: Long, amount: Double, note: String) {
        viewModelScope.launch {
            repository.addCustomerDue(customerId, amount, note)
        }
    }

    fun sendSmsReminder(context: Context, customer: CustomerEntity) {
        val message = "প্রিয় ${customer.name}, ${shopName.value} -এ আপনার বর্তমান মোট বাকি ৳${String.format("%.2f", customer.totalDue)} টাকা। অনুগ্রহ করে দ্রুত পরিশোধের অনুরোধ জানাচ্ছি। ধন্যবাদ!"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("sms:${customer.phone}")
            putExtra("sms_body", message)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "SMS অ্যাপ খোলা যায়নি: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendWhatsAppBill(context: Context, customer: CustomerEntity) {
        val message = "আসসালামু আলাইকুম ${customer.name} ভাই/আপা,\n\n${shopName.value} থেকে আপনার হিসাবের বিবরনী:\nবর্তমান মোট বাকি: ৳${String.format("%.2f", customer.totalDue)} টাকা।\n\nbKash/Nagad মাধ্যমেও পরিশোধ করতে পারেন। ধন্যবাদ!"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            val url = "https://api.whatsapp.com/send?phone=88${customer.phone}&text=" + Uri.encode(message)
            data = Uri.parse(url)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp খোলা যায়নি: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Medicine Actions
    fun addOrUpdateMedicine(medicine: MedicineEntity) {
        viewModelScope.launch {
            if (medicine.id == 0L) {
                repository.addMedicine(medicine)
            } else {
                repository.updateMedicine(medicine)
            }
        }
    }

    fun deleteMedicine(medicine: MedicineEntity) {
        viewModelScope.launch {
            repository.deleteMedicine(medicine)
        }
    }

    // POS Cart Actions
    fun addToCart(medicine: MedicineEntity) {
        val current = cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.medicine.id == medicine.id }
        if (index >= 0) {
            val existing = current[index]
            if (existing.quantity < medicine.stockQuantity) {
                current[index] = existing.copy(quantity = existing.quantity + 1)
            }
        } else {
            if (medicine.stockQuantity > 0) {
                current.add(CartItem(medicine = medicine, quantity = 1))
            }
        }
        cartItems.value = current
    }

    fun updateCartQuantity(medicineId: Long, newQuantity: Int) {
        val current = cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.medicine.id == medicineId }
        if (index >= 0) {
            if (newQuantity <= 0) {
                current.removeAt(index)
            } else {
                val med = current[index].medicine
                val cappedQty = newQuantity.coerceAtMost(med.stockQuantity)
                current[index] = current[index].copy(quantity = cappedQty)
            }
            cartItems.value = current
        }
    }

    fun clearCart() {
        cartItems.value = emptyList()
        selectedCustomer.value = null
        discountInput.value = "0"
        paidInput.value = "0"
    }

    fun checkoutPos(onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val cart = cartItems.value
            if (cart.isEmpty()) return@launch

            val discount = discountInput.value.toDoubleOrNull() ?: 0.0
            val paid = paidInput.value.toDoubleOrNull() ?: 0.0
            val cust = selectedCustomer.value

            val invoiceId = repository.completePosSale(
                customerId = cust?.id,
                customerName = cust?.name ?: "সাধারণ কাস্টমার",
                customerPhone = cust?.phone ?: "",
                cartItems = cart,
                discountAmount = discount,
                paidAmount = paid,
                paymentMethod = paymentMethodInput.value,
                employeeName = currentEmployee.value
            )

            clearCart()
            onSuccess(invoiceId)
        }
    }

    // Cash Box Actions
    fun setOpeningBalance(amount: Double) {
        viewModelScope.launch {
            repository.setOpeningBalance(amount)
        }
    }

    fun addExpense(title: String, amount: Double, category: String) {
        viewModelScope.launch {
            repository.addExpense(title, amount, category)
        }
    }

    fun addCashIn(title: String, amount: Double, category: String) {
        viewModelScope.launch {
            repository.addCashIn(title, amount, category)
        }
    }

    fun reconcileAndCloseCashBox(actualCashHand: Double) {
        viewModelScope.launch {
            repository.reconcileAndCloseCashBox(actualCashHand)
        }
    }
}

class PharmacyViewModelFactory(private val repository: PharmacyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PharmacyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PharmacyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
