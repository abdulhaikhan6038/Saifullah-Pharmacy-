package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CashTransactionEntity
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen40
import com.example.ui.theme.ForestGreenDeep
import com.example.ui.viewmodel.PharmacyViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashBoxScreen(
    viewModel: PharmacyViewModel,
    modifier: Modifier = Modifier
) {
    val cashBox by viewModel.latestCashBox.collectAsState()
    val transactions by viewModel.cashTransactions.collectAsState()

    var showExpenseDialog by remember { mutableStateOf(false) }
    var showCashInDialog by remember { mutableStateOf(false) }
    var showOpeningBalanceDialog by remember { mutableStateOf(false) }
    var showReconcileDialog by remember { mutableStateOf(false) }

    val opening = cashBox?.openingBalance ?: 0.0
    val sales = cashBox?.cashSales ?: 0.0
    val cashIn = cashBox?.cashIn ?: 0.0
    val expenses = cashBox?.cashExpenses ?: 0.0
    val calculatedClosing = opening + sales + cashIn - expenses

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Title & Opening balance
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ক্যাশ বাক্স (Cash Box)",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "তারিখ: ${cashBox?.dateString ?: "আজ"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { showReconcileDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen40),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("reconcile_cash_button")
            ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = "ক্যাশ মিলানো")
                Spacer(modifier = Modifier.width(4.dp))
                Text("ক্যাশ মিলানো")
            }
        }

        // Summary Card Grid
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("হিসাব অনুযায়ী বর্তমান ক্যাশ", style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = "৳ ${String.format("%.2f", calculatedClosing)}",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenDeep
                            )
                        )
                    }

                    OutlinedButton(
                        onClick = { showOpeningBalanceDialog = true },
                        modifier = Modifier.testTag("edit_opening_btn")
                    ) {
                        Text("ওপেনিং পরিবর্তন", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricMiniBox("ওপেনিং ক্যাশ", "৳${String.format("%.0f", opening)}", EmeraldGreen40)
                    MetricMiniBox("নগদ বিক্রি", "+ ৳${String.format("%.0f", sales)}", EmeraldGreen40)
                    MetricMiniBox("ক্যাশ জমা", "+ ৳${String.format("%.0f", cashIn)}", ForestGreenDeep)
                    MetricMiniBox("খরচ", "- ৳${String.format("%.0f", expenses)}", CrimsonRed)
                }

                // Show Short / Over result if reconciled
                cashBox?.shortOrOverAmount?.let { diff ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = if (diff == 0.0) EmeraldGreen40.copy(alpha = 0.15f) else if (diff < 0) CrimsonRed.copy(alpha = 0.15f) else AmberWarning.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (diff == 0.0) "✅ ক্যাশ সঠিকভাবে মিলেছে!" else if (diff < 0) "⚠️ ক্যাশ শর্ট (কম): ৳${String.format("%.2f", Math.abs(diff))}" else "ℹ️ ক্যাশ ওভার (বেশি): ৳${String.format("%.2f", diff)}",
                                fontWeight = FontWeight.Bold,
                                color = if (diff == 0.0) EmeraldGreen40 else if (diff < 0) CrimsonRed else AmberWarning
                            )
                            Text("হাতে ক্যাশ: ৳${cashBox?.closingBalanceActual}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Quick Expense / Deposit Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { showExpenseDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("add_expense_button")
            ) {
                Icon(Icons.Default.Remove, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("খরচ লিখুন")
            }

            Button(
                onClick = { showCashInDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen40),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("add_cash_in_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("ক্যাশ জমা লিখুন")
            }
        }

        // Transactions History
        Text("আজকের ক্যাশ লেনদেনের ইতিহাস:", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("আজকের ক্যাশে কোন লেনদেন হয়নি", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(transactions) { tx ->
                    CashTxRowItem(tx)
                }
            }
        }
    }

    // Expense Dialog
    if (showExpenseDialog) {
        var title by remember { mutableStateOf("") }
        var amountText by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("চা/নাস্তা") }

        AlertDialog(
            onDismissRequest = { showExpenseDialog = false },
            title = { Text("নতুন খরচ যোগ করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("খরচের বিবরণ (উদা: দোকানের চা নাস্তা)") },
                        modifier = Modifier.fillMaxWidth().testTag("expense_title_input")
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("টাকার পরিমাণ *") },
                        modifier = Modifier.fillMaxWidth().testTag("expense_amount_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0 && title.isNotBlank()) {
                            viewModel.addExpense(title, amt, category)
                            showExpenseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    modifier = Modifier.testTag("save_expense_button")
                ) {
                    Text("খরচ যুক্ত করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExpenseDialog = false }) { Text("বাতিল") }
            }
        )
    }

    // Cash In Dialog
    if (showCashInDialog) {
        var title by remember { mutableStateOf("") }
        var amountText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCashInDialog = false },
            title = { Text("ক্যাশ জমা যোগ করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("জমার বিবরণ (উদা: মূলধন জমা)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("টাকার পরিমাণ *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.addCashIn(if (title.isBlank()) "ক্যাশ জমা" else title, amt, "সাধারণ")
                            showCashInDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen40)
                ) {
                    Text("জমা যুক্ত করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCashInDialog = false }) { Text("বাতিল") }
            }
        )
    }

    // Opening Balance Dialog
    if (showOpeningBalanceDialog) {
        var openingText by remember { mutableStateOf(opening.toString()) }

        AlertDialog(
            onDismissRequest = { showOpeningBalanceDialog = false },
            title = { Text("দিনের ওপেনিং ব্যালেন্স সেট করুন") },
            text = {
                OutlinedTextField(
                    value = openingText,
                    onValueChange = { openingText = it },
                    label = { Text("ক্যাশ বাক্সে শুরুর ব্যালেন্স (৳)") },
                    modifier = Modifier.fillMaxWidth().testTag("opening_balance_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = openingText.toDoubleOrNull() ?: 0.0
                        viewModel.setOpeningBalance(amt)
                        showOpeningBalanceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen40)
                ) {
                    Text("সেভ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOpeningBalanceDialog = false }) { Text("বাতিল") }
            }
        )
    }

    // Reconcile Dialog (ক্যাশ মিলানো)
    if (showReconcileDialog) {
        var actualHandText by remember { mutableStateOf("") }
        val actualAmt = actualHandText.toDoubleOrNull() ?: 0.0
        val diff = actualAmt - calculatedClosing

        AlertDialog(
            onDismissRequest = { showReconcileDialog = false },
            title = { Text("দিনের শেষে ক্যাশ মিলানো (Reconciliation)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("হিসাব অনুযায়ী বাক্সে থাকার কথা: ৳${String.format("%.2f", calculatedClosing)}", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = actualHandText,
                        onValueChange = { actualHandText = it },
                        label = { Text("বাস্তবে হাতে/বাক্সে গণনাকৃত ক্যাশ (৳) *") },
                        modifier = Modifier.fillMaxWidth().testTag("actual_cash_input")
                    )

                    if (actualHandText.isNotBlank()) {
                        Surface(
                            color = if (diff == 0.0) EmeraldGreen40.copy(alpha = 0.2f) else if (diff < 0) CrimsonRed.copy(alpha = 0.2f) else AmberWarning.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (diff == 0.0) "✅ ক্যাশ হুবহু মিলেছে!" else if (diff < 0) "⚠️ শর্ট/ঘাটতি: ৳${String.format("%.2f", Math.abs(diff))}" else "ℹ️ ওভার/উদ্বৃত্ত: ৳${String.format("%.2f", diff)}",
                                modifier = Modifier.padding(10.dp),
                                fontWeight = FontWeight.Bold,
                                color = if (diff == 0.0) EmeraldGreen40 else if (diff < 0) CrimsonRed else AmberWarning
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (actualHandText.isNotBlank()) {
                            viewModel.reconcileAndCloseCashBox(actualAmt)
                            showReconcileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen40),
                    modifier = Modifier.testTag("save_reconcile_button")
                ) {
                    Text("মিলিয়ে ক্লোজ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReconcileDialog = false }) { Text("বাতিল") }
            }
        )
    }
}

@Composable
fun MetricMiniBox(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = color))
    }
}

@Composable
fun CashTxRowItem(tx: CashTransactionEntity) {
    val isOut = tx.type == "EXPENSE" || tx.type == "CASH_OUT"
    ElevatedCard(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isOut) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (isOut) CrimsonRed else EmeraldGreen40,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(tx.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    Text(tx.type, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Text(
                text = "${if (isOut) "-" else "+"} ৳${String.format("%.2f", tx.amount)}",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isOut) CrimsonRed else EmeraldGreen40
                )
            )
        }
    }
}
