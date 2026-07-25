package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CustomerEntity
import com.example.data.entity.CustomerTransactionEntity
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen40
import com.example.ui.theme.ForestGreenDeep
import com.example.ui.viewmodel.PharmacyViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerLedgerScreen(
    viewModel: PharmacyViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customers by viewModel.allCustomers.collectAsState()
    val totalDues by viewModel.totalDues.collectAsState()
    val searchQuery by viewModel.customerQuery.collectAsState()

    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var selectedCustomerForDetails by remember { mutableStateOf<CustomerEntity?>(null) }
    var showPaymentDialogForCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var showDueDialogForCustomer by remember { mutableStateOf<CustomerEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Top Total Dues Banner Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "মোট কাস্টমার বাকি",
                        style = MaterialTheme.typography.titleMedium,
                        color = ForestGreenDeep
                    )
                    Text(
                        text = "৳ ${String.format("%.2f", totalDues)}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = CrimsonRed
                        )
                    )
                    Text(
                        text = "মোট কাস্টমার: ${customers.size} জন",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddCustomerDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen40),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_customer_button")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "নতুন কাস্টমার")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("নতুন কাস্টমার")
                }
            }
        }

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.customerQuery.value = it },
            placeholder = { Text("কাস্টমারের নাম বা ফোন নম্বর খুঁজুন...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.customerQuery.value = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("search_customer_input")
        )

        // Customer List
        if (customers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AccountBox,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (searchQuery.isBlank()) "কোন কাস্টমার যুক্ত নেই" else "কোন কাস্টমার পাওয়া যায়নি",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(customers, key = { it.id }) { customer ->
                    CustomerCardItem(
                        customer = customer,
                        onCardClick = { selectedCustomerForDetails = customer },
                        onPaymentClick = { showPaymentDialogForCustomer = customer },
                        onDueClick = { showDueDialogForCustomer = customer },
                        onSmsClick = { viewModel.sendSmsReminder(context, customer) },
                        onWhatsAppClick = { viewModel.sendWhatsAppBill(context, customer) }
                    )
                }
            }
        }
    }

    // Add Customer Dialog
    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onDismiss = { showAddCustomerDialog = false },
            onSave = { name, phone, address ->
                viewModel.addCustomer(name, phone, address)
                showAddCustomerDialog = false
            }
        )
    }

    // Customer Payment Dialog (টাকা জমা)
    showPaymentDialogForCustomer?.let { customer ->
        PaymentDialog(
            customer = customer,
            onDismiss = { showPaymentDialogForCustomer = null },
            onConfirm = { amount, method, note ->
                viewModel.recordCustomerPayment(customer.id, amount, method, note)
                showPaymentDialogForCustomer = null
            }
        )
    }

    // Add Due Dialog (নতুন বাকি)
    showDueDialogForCustomer?.let { customer ->
        AddDueDialog(
            customer = customer,
            onDismiss = { showDueDialogForCustomer = null },
            onConfirm = { amount, note ->
                viewModel.addCustomerDue(customer.id, amount, note)
                showDueDialogForCustomer = null
            }
        )
    }

    // Customer Details Sheet
    selectedCustomerForDetails?.let { customer ->
        CustomerDetailsDialog(
            customer = customer,
            viewModel = viewModel,
            context = context,
            onDismiss = { selectedCustomerForDetails = null },
            onPayClick = {
                showPaymentDialogForCustomer = customer
                selectedCustomerForDetails = null
            }
        )
    }
}

@Composable
fun CustomerCardItem(
    customer: CustomerEntity,
    onCardClick: () -> Unit,
    onPaymentClick: () -> Unit,
    onDueClick: () -> Unit,
    onSmsClick: () -> Unit,
    onWhatsAppClick: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("customer_item_${customer.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen40.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = customer.name.take(1),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen40
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = customer.phone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (customer.totalDue > 0) "৳ ${String.format("%.2f", customer.totalDue)}" else "পরিশোধিত",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (customer.totalDue > 0) CrimsonRed else EmeraldGreen40
                        )
                    )
                    Text(
                        text = if (customer.totalDue > 0) "মোট বাকি" else "বাকি নেই",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (customer.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = customer.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = onPaymentClick,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = EmeraldGreen40.copy(alpha = 0.15f),
                            contentColor = EmeraldGreen40
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("pay_button_${customer.id}")
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("টাকা জমা", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onDueClick,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_due_button_${customer.id}")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("নতুন বাকি", fontSize = 12.sp)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onSmsClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Message,
                            contentDescription = "SMS নোটিফিকেশন",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onWhatsAppClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "WhatsApp বিল পাঠাইন",
                            tint = EmeraldGreen40
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন কাস্টমার যোগ করুন") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("কাস্টমারের নাম *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_name_input")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_phone_input")
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("ঠিকানা (ঐচ্ছিক)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, phone, address) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen40),
                modifier = Modifier.testTag("save_customer_button")
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

@Composable
fun PaymentDialog(
    customer: CustomerEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("CASH") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${customer.name} - টাকা জমা") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("বর্তমান মোট বাকি: ৳${String.format("%.2f", customer.totalDue)}", color = CrimsonRed, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("জমার পরিমাণ (টাকা) *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_amount_input")
                )

                Text("পেমেন্ট মেথড:", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("CASH" to "নগদ", "BKASH" to "বিকাশ", "NAGAD" to "নগদ (ডিপোজিট)", "BANK" to "ব্যাংক").forEach { (code, label) ->
                        FilterChip(
                            selected = selectedMethod == code,
                            onClick = { selectedMethod = code },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("নোট / রসিদ নম্বর") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) onConfirm(amt, selectedMethod, note)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen40),
                modifier = Modifier.testTag("confirm_payment_button")
            ) {
                Text("জমা নিশ্চিত করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

@Composable
fun AddDueDialog(
    customer: CustomerEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${customer.name} - নতুন বাকি হিসাব") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("নতুন বাকির পরিমাণ (টাকা) *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("বিবরণ (উদা: নাপা ২ পাতা বাকিতে)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) onConfirm(amt, note)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
            ) {
                Text("বাকি যোগ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

@Composable
fun CustomerDetailsDialog(
    customer: CustomerEntity,
    viewModel: PharmacyViewModel,
    context: Context,
    onDismiss: () -> Unit,
    onPayClick: () -> Unit
) {
    val transactions: List<CustomerTransactionEntity> by viewModel.getTransactionsForCustomer(customer.id).collectAsState(initial = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(customer.name, fontWeight = FontWeight.Bold)
                Text(customer.phone, style = MaterialTheme.typography.bodySmall)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("মোট বাকি", style = MaterialTheme.typography.labelSmall)
                            Text("৳ ${String.format("%.2f", customer.totalDue)}", color = CrimsonRed, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("মোট জমা", style = MaterialTheme.typography.labelSmall)
                            Text("৳ ${String.format("%.2f", customer.totalPaid)}", color = EmeraldGreen40, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text("লেনদেনের ইতিহাস:", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 6.dp))

                if (transactions.isEmpty()) {
                    Text("কোন পূর্ববর্তী লেনদেন রেকর্ড করা হয়নি।", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(transactions) { tx ->
                            TransactionItemRow(tx)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onPayClick,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen40)
            ) {
                Text("টাকা জমা দিন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বন্ধ করুন") }
        }
    )
}

@Composable
fun TransactionItemRow(tx: CustomerTransactionEntity) {
    val isPayment = tx.type == "PAYMENT"
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isPayment) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isPayment) "জমা (পরিশোধ)" else "বাকি গ্রহণ",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isPayment) EmeraldGreen40 else CrimsonRed
                )
                if (tx.note.isNotBlank()) {
                    Text(tx.note, style = MaterialTheme.typography.labelSmall)
                }
            }
            Text(
                text = "${if (isPayment) "-" else "+"} ৳${String.format("%.2f", tx.amount)}",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = if (isPayment) EmeraldGreen40 else CrimsonRed
            )
        }
    }
}
