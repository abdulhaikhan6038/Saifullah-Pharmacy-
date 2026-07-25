package com.example.data

import com.example.data.dao.*
import com.example.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class PharmacyRepository(
    private val customerDao: CustomerDao,
    private val customerTransactionDao: CustomerTransactionDao,
    private val medicineDao: MedicineDao,
    private val saleInvoiceDao: SaleInvoiceDao,
    private val cashBoxDao: CashBoxDao
) {
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    val totalDuesFlow: Flow<Double?> = customerDao.getTotalDuesFlow()
    val allMedicines: Flow<List<MedicineEntity>> = medicineDao.getAllMedicines()
    val lowStockMedicines: Flow<List<MedicineEntity>> = medicineDao.getLowStockMedicines()
    val allInvoices: Flow<List<SaleInvoiceEntity>> = saleInvoiceDao.getAllInvoices()
    val latestCashBox: Flow<CashBoxEntity?> = cashBoxDao.getLatestCashBoxFlow()
    val allCashTransactions: Flow<List<CashTransactionEntity>> = cashBoxDao.getAllCashTransactions()

    suspend fun seedSampleDataIfEmpty() = withContext(Dispatchers.IO) {
        val currentMedicines = medicineDao.getAllMedicines().firstOrNull()
        if (currentMedicines.isNullOrEmpty()) {
            val sampleMeds = listOf(
                MedicineEntity(name = "নাপা ৫০০ মিগ্রা (Napa 500mg)", barcode = "8901001", batchNumber = "NP2026A", expiryDate = "2027-06-30", buyPrice = 1.0, sellPrice = 1.2, stockQuantity = 180, minStockAlert = 20, category = "ট্যাবলেট"),
                MedicineEntity(name = "সিকলো ২০ মিগ্রা (Seclo 20mg)", barcode = "8901002", batchNumber = "SC2026B", expiryDate = "2026-12-15", buyPrice = 5.0, sellPrice = 6.0, stockQuantity = 95, minStockAlert = 15, category = "ক্যাপসুল"),
                MedicineEntity(name = "ম্যাক্সপ্রো ২০ মিগ্রা (Maxpro 20mg)", barcode = "8901003", batchNumber = "MP2026C", expiryDate = "2027-08-20", buyPrice = 6.5, sellPrice = 8.0, stockQuantity = 140, minStockAlert = 20, category = "ট্যাবলেট"),
                MedicineEntity(name = "এস সিরাপ ১০০ মি.লি (Ace Syrup)", barcode = "8901004", batchNumber = "AC2026D", expiryDate = "2026-08-10", buyPrice = 30.0, sellPrice = 35.0, stockQuantity = 14, minStockAlert = 10, category = "সিরাপ"),
                MedicineEntity(name = "সিপ্রোসিন ৫০০ মিগ্রা (Ciprocin)", barcode = "8901005", batchNumber = "CP2026E", expiryDate = "2027-02-28", buyPrice = 12.0, sellPrice = 15.0, stockQuantity = 5, minStockAlert = 10, category = "ট্যাবলেট"),
                MedicineEntity(name = "সারজেল ২০ মিগ্রা (Sergel 20mg)", barcode = "8901006", batchNumber = "SG2026F", expiryDate = "2027-10-10", buyPrice = 6.0, sellPrice = 7.0, stockQuantity = 220, minStockAlert = 25, category = "ক্যাপসুল"),
                MedicineEntity(name = "অফসেপ্ট আই ড্রপ (Ofsept Eye Drop)", barcode = "8901007", batchNumber = "OF2026G", expiryDate = "2026-08-25", buyPrice = 45.0, sellPrice = 55.0, stockQuantity = 8, minStockAlert = 5, category = "ড্রপ")
            )
            sampleMeds.forEach { medicineDao.insertMedicine(it) }
        }

        val currentCustomers = customerDao.getAllCustomers().firstOrNull()
        if (currentCustomers.isNullOrEmpty()) {
            val c1 = customerDao.insertCustomer(CustomerEntity(name = "মোঃ রফিকুল ইসলাম", phone = "01711223344", address = "মিরপুর-১০, ঢাকা", totalDue = 450.0, totalPaid = 1200.0))
            val c2 = customerDao.insertCustomer(CustomerEntity(name = "আব্দুর রহিম", phone = "01812345678", address = "ধানমন্ডি, ঢাকা", totalDue = 180.0, totalPaid = 800.0))
            val c3 = customerDao.insertCustomer(CustomerEntity(name = "পারভীন আক্তার", phone = "01998765432", address = "উত্তরা, ঢাকা", totalDue = 0.0, totalPaid = 2500.0))

            customerTransactionDao.insertTransaction(CustomerTransactionEntity(customerId = c1, customerName = "মোঃ রফিকুল ইসলাম", amount = 450.0, type = "DUE", note = "ওষুধ বাকিতে বিক্রয়", paymentMethod = "CASH"))
            customerTransactionDao.insertTransaction(CustomerTransactionEntity(customerId = c2, customerName = "আব্দুর রহিম", amount = 180.0, type = "DUE", note = "জরুরী ইনসুলিন বাকিতে", paymentMethod = "CASH"))
        }

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var cb = cashBoxDao.getCashBoxForDate(todayDate)
        if (cb == null) {
            cb = CashBoxEntity(
                dateString = todayDate,
                openingBalance = 5000.0,
                cashSales = 2450.0,
                cashExpenses = 350.0,
                cashIn = 500.0,
                cashOut = 0.0,
                closingBalanceCalculated = 7600.0,
                isClosed = false
            )
            cashBoxDao.insertOrUpdateCashBox(cb)

            cashBoxDao.insertCashTransaction(CashTransactionEntity(type = "EXPENSE", title = "দোকানের চা/নাস্তা খরচ", amount = 150.0, category = "নাস্তা"))
            cashBoxDao.insertCashTransaction(CashTransactionEntity(type = "EXPENSE", title = "প্যাকেজিং পলি ক্রয়", amount = 200.0, category = "প্যাকেজিং"))
            cashBoxDao.insertCashTransaction(CashTransactionEntity(type = "CASH_IN", title = "মালিকের অতিরিক্ত ক্যাশ জমা", amount = 500.0, category = "মূলধন"))
        }
    }

    // Customer Operations
    fun searchCustomers(query: String): Flow<List<CustomerEntity>> = customerDao.searchCustomers(query)
    suspend fun getCustomerById(id: Long) = customerDao.getCustomerById(id)
    suspend fun addCustomer(customer: CustomerEntity): Long = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: CustomerEntity) = customerDao.updateCustomer(customer)

    fun getTransactionsForCustomer(customerId: Long): Flow<List<CustomerTransactionEntity>> =
        customerTransactionDao.getTransactionsForCustomer(customerId)

    suspend fun recordCustomerPayment(customerId: Long, amount: Double, paymentMethod: String, note: String) = withContext(Dispatchers.IO) {
        val customer = customerDao.getCustomerById(customerId) ?: return@withContext
        val newDue = (customer.totalDue - amount).coerceAtLeast(0.0)
        val newPaid = customer.totalPaid + amount
        val updatedCustomer = customer.copy(totalDue = newDue, totalPaid = newPaid, updatedAt = System.currentTimeMillis())
        customerDao.updateCustomer(updatedCustomer)

        customerTransactionDao.insertTransaction(
            CustomerTransactionEntity(
                customerId = customerId,
                customerName = customer.name,
                amount = amount,
                type = "PAYMENT",
                note = if (note.isBlank()) "বাকি টাকা জমা" else note,
                paymentMethod = paymentMethod
            )
        )

        // Record in cash box if cash payment
        if (paymentMethod == "CASH") {
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val cb = cashBoxDao.getCashBoxForDate(todayDate)
            if (cb != null) {
                val updatedIn = cb.cashIn + amount
                val updatedCalc = cb.openingBalance + cb.cashSales + updatedIn - cb.cashExpenses - cb.cashOut
                cashBoxDao.insertOrUpdateCashBox(cb.copy(cashIn = updatedIn, closingBalanceCalculated = updatedCalc, updatedAt = System.currentTimeMillis()))
            }
            cashBoxDao.insertCashTransaction(
                CashTransactionEntity(type = "DUE_COLLECTION", title = "${customer.name} - বাকি পরিশোধ", amount = amount, paymentMethod = paymentMethod)
            )
        }
    }

    suspend fun addCustomerDue(customerId: Long, amount: Double, note: String) = withContext(Dispatchers.IO) {
        val customer = customerDao.getCustomerById(customerId) ?: return@withContext
        val newDue = customer.totalDue + amount
        val updatedCustomer = customer.copy(totalDue = newDue, updatedAt = System.currentTimeMillis())
        customerDao.updateCustomer(updatedCustomer)

        customerTransactionDao.insertTransaction(
            CustomerTransactionEntity(
                customerId = customerId,
                customerName = customer.name,
                amount = amount,
                type = "DUE",
                note = if (note.isBlank()) "নতুন বাকি হিসাব" else note,
                paymentMethod = "CASH"
            )
        )
    }

    // Medicine Operations
    fun searchMedicines(query: String): Flow<List<MedicineEntity>> = medicineDao.searchMedicines(query)
    suspend fun getMedicineById(id: Long) = medicineDao.getMedicineById(id)
    suspend fun getMedicineByBarcode(barcode: String) = medicineDao.getMedicineByBarcode(barcode)
    suspend fun addMedicine(medicine: MedicineEntity): Long = medicineDao.insertMedicine(medicine)
    suspend fun updateMedicine(medicine: MedicineEntity) = medicineDao.updateMedicine(medicine)
    suspend fun deleteMedicine(medicine: MedicineEntity) = medicineDao.deleteMedicine(medicine)

    // POS & Sales Invoice
    suspend fun getInvoiceItems(invoiceId: Long): List<SaleItemEntity> = saleInvoiceDao.getItemsForInvoice(invoiceId)

    suspend fun completePosSale(
        customerId: Long?,
        customerName: String,
        customerPhone: String,
        cartItems: List<CartItem>,
        discountAmount: Double,
        paidAmount: Double,
        paymentMethod: String, // CASH, BKASH, NAGAD, BANK, DUE
        employeeName: String
    ): Long = withContext(Dispatchers.IO) {
        val rawTotal = cartItems.sumOf { it.medicine.sellPrice * it.quantity }
        val netTotal = (rawTotal - discountAmount).coerceAtLeast(0.0)
        val due = (netTotal - paidAmount).coerceAtLeast(0.0)

        val invNumber = "INV-" + (System.currentTimeMillis() % 1000000)

        val invoiceEntity = SaleInvoiceEntity(
            invoiceNumber = invNumber,
            customerId = customerId,
            customerName = if (customerName.isBlank()) "সাধারণ কাস্টমার" else customerName,
            customerPhone = customerPhone,
            totalAmount = rawTotal,
            discountAmount = discountAmount,
            netAmount = netTotal,
            paidAmount = paidAmount,
            dueAmount = due,
            paymentMethod = paymentMethod,
            employeeName = employeeName,
            timestamp = System.currentTimeMillis()
        )

        val invoiceId = saleInvoiceDao.insertInvoice(invoiceEntity)

        val saleItems = cartItems.map { cart ->
            SaleItemEntity(
                invoiceId = invoiceId,
                medicineId = cart.medicine.id,
                medicineName = cart.medicine.name,
                batchNumber = cart.medicine.batchNumber,
                quantity = cart.quantity,
                unitPrice = cart.medicine.sellPrice,
                unitBuyPrice = cart.medicine.buyPrice,
                subtotal = cart.medicine.sellPrice * cart.quantity
            )
        }
        saleInvoiceDao.insertSaleItems(saleItems)

        // Decrement stock for each medicine
        cartItems.forEach { cart ->
            val updatedStock = (cart.medicine.stockQuantity - cart.quantity).coerceAtLeast(0)
            medicineDao.updateMedicine(cart.medicine.copy(stockQuantity = updatedStock, updatedAt = System.currentTimeMillis()))
        }

        // If due > 0 and customer is linked, update customer due balance
        if (due > 0 && customerId != null) {
            val customer = customerDao.getCustomerById(customerId)
            if (customer != null) {
                val newDueBalance = customer.totalDue + due
                customerDao.updateCustomer(customer.copy(totalDue = newDueBalance, updatedAt = System.currentTimeMillis()))

                customerTransactionDao.insertTransaction(
                    CustomerTransactionEntity(
                        customerId = customerId,
                        customerName = customer.name,
                        amount = due,
                        type = "DUE",
                        note = "বিক্রয় ইনভয়েস #$invNumber বাকি",
                        paymentMethod = paymentMethod
                    )
                )
            }
        }

        // Update Cash Box if paid with cash
        if (paymentMethod == "CASH" && paidAmount > 0) {
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            var cb = cashBoxDao.getCashBoxForDate(todayDate)
            if (cb == null) {
                cb = CashBoxEntity(dateString = todayDate, openingBalance = 0.0)
            }
            val newSales = cb.cashSales + paidAmount
            val calcClosing = cb.openingBalance + newSales + cb.cashIn - cb.cashExpenses - cb.cashOut
            cashBoxDao.insertOrUpdateCashBox(cb.copy(cashSales = newSales, closingBalanceCalculated = calcClosing, updatedAt = System.currentTimeMillis()))

            cashBoxDao.insertCashTransaction(
                CashTransactionEntity(type = "SALE", title = "ইনভয়েস #$invNumber বিক্রি", amount = paidAmount, paymentMethod = paymentMethod)
            )
        }

        return@withContext invoiceId
    }

    // Cash Box Operations
    suspend fun getTodayCashBox(): CashBoxEntity = withContext(Dispatchers.IO) {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var cb = cashBoxDao.getCashBoxForDate(todayDate)
        if (cb == null) {
            cb = CashBoxEntity(dateString = todayDate, openingBalance = 0.0, closingBalanceCalculated = 0.0)
            val id = cashBoxDao.insertOrUpdateCashBox(cb)
            cb = cb.copy(id = id)
        }
        return@withContext cb
    }

    suspend fun setOpeningBalance(openingAmount: Double) = withContext(Dispatchers.IO) {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var cb = cashBoxDao.getCashBoxForDate(todayDate) ?: CashBoxEntity(dateString = todayDate)
        val calcClosing = openingAmount + cb.cashSales + cb.cashIn - cb.cashExpenses - cb.cashOut
        cashBoxDao.insertOrUpdateCashBox(cb.copy(openingBalance = openingAmount, closingBalanceCalculated = calcClosing, updatedAt = System.currentTimeMillis()))
    }

    suspend fun addExpense(title: String, amount: Double, category: String) = withContext(Dispatchers.IO) {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var cb = cashBoxDao.getCashBoxForDate(todayDate) ?: CashBoxEntity(dateString = todayDate)
        val newExpenses = cb.cashExpenses + amount
        val calcClosing = cb.openingBalance + cb.cashSales + cb.cashIn - newExpenses - cb.cashOut
        cashBoxDao.insertOrUpdateCashBox(cb.copy(cashExpenses = newExpenses, closingBalanceCalculated = calcClosing, updatedAt = System.currentTimeMillis()))

        cashBoxDao.insertCashTransaction(CashTransactionEntity(type = "EXPENSE", title = title, amount = amount, category = category))
    }

    suspend fun addCashIn(title: String, amount: Double, category: String) = withContext(Dispatchers.IO) {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var cb = cashBoxDao.getCashBoxForDate(todayDate) ?: CashBoxEntity(dateString = todayDate)
        val newCashIn = cb.cashIn + amount
        val calcClosing = cb.openingBalance + cb.cashSales + newCashIn - cb.cashExpenses - cb.cashOut
        cashBoxDao.insertOrUpdateCashBox(cb.copy(cashIn = newCashIn, closingBalanceCalculated = calcClosing, updatedAt = System.currentTimeMillis()))

        cashBoxDao.insertCashTransaction(CashTransactionEntity(type = "CASH_IN", title = title, amount = amount, category = category))
    }

    suspend fun reconcileAndCloseCashBox(actualCashHand: Double) = withContext(Dispatchers.IO) {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val cb = cashBoxDao.getCashBoxForDate(todayDate) ?: return@withContext
        val calculated = cb.closingBalanceCalculated
        val shortOrOver = actualCashHand - calculated

        val updated = cb.copy(
            closingBalanceActual = actualCashHand,
            shortOrOverAmount = shortOrOver,
            isClosed = true,
            updatedAt = System.currentTimeMillis()
        )
        cashBoxDao.insertOrUpdateCashBox(updated)
    }
}

data class CartItem(
    val medicine: MedicineEntity,
    var quantity: Int = 1
)
