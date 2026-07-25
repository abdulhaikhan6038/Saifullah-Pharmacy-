package com.example.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen40
import com.example.ui.theme.ForestGreenDeep
import com.example.ui.viewmodel.PharmacyViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: PharmacyViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val invoices by viewModel.allInvoices.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()
    val totalDues by viewModel.totalDues.collectAsState()
    val cashBox by viewModel.latestCashBox.collectAsState()

    var timeframeIndex by remember { mutableIntStateOf(0) } // 0: আজ, 1: চলতি সপ্তাহ, 2: চলতি মাস
    var showPdfReportPreview by remember { mutableStateOf(false) }

    // Calculations
    val totalSales = invoices.sumOf { it.netAmount }
    val totalExpenses = cashBox?.cashExpenses ?: 0.0
    val totalCostOfGoods = totalSales * 0.78 // approx 22% average margin
    val netProfit = (totalSales - totalCostOfGoods - totalExpenses).coerceAtLeast(0.0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Title & Export Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ফার্মেসি রিপোর্ট ও অ্যানালিটিক্স",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "বিক্রয়, লাভ-লোকসান ও বাকি খতিয়ান",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { showPdfReportPreview = true },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen40),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("export_pdf_button")
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF/প্রিন্ট")
                Spacer(modifier = Modifier.width(4.dp))
                Text("PDF রিপোর্ট")
            }
        }

        // Timeframe selector
        SecondaryTabRow(
            selectedTabIndex = timeframeIndex,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Tab(selected = timeframeIndex == 0, onClick = { timeframeIndex = 0 }, text = { Text("আজকের রিপোর্ট") })
            Tab(selected = timeframeIndex == 1, onClick = { timeframeIndex = 1 }, text = { Text("চলতি সপ্তাহ") })
            Tab(selected = timeframeIndex == 2, onClick = { timeframeIndex = 2 }, text = { Text("চলতি মাস") })
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Profit & Loss Card
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("আনুমানিক নিট লাভ (Profit)", style = MaterialTheme.typography.titleMedium, color = ForestGreenDeep)
                                Text(
                                    text = "৳ ${String.format("%.2f", netProfit)}",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldGreen40)
                                )
                            }

                            Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(36.dp), tint = EmeraldGreen40)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ReportMiniStat("মোট বিক্রি", "৳${String.format("%.0f", totalSales)}", EmeraldGreen40)
                            ReportMiniStat("আনুমানিক কেনা দাম", "৳${String.format("%.0f", totalCostOfGoods)}", MaterialTheme.colorScheme.onSurfaceVariant)
                            ReportMiniStat("দোকান খরচ", "৳${String.format("%.0f", totalExpenses)}", CrimsonRed)
                            ReportMiniStat("কাস্টমার বাকি", "৳${String.format("%.0f", totalDues)}", CrimsonRed)
                        }
                    }
                }
            }

            // Top Selling Medicines
            item {
                Text("সবচেয়ে বেশি বিক্রি হওয়া ওষুধ:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        listOf(
                            Triple("নাপা ৫০০ মিগ্রা (Napa 500mg)", "২৪০ পাতা বিক্রি", "৳ ২৮৮.০০"),
                            Triple("সিকলো ২০ মিগ্রা (Seclo 20mg)", "১৪৫ ক্যাপসুল বিক্রি", "৳ ৮৭০.০০"),
                            Triple("ম্যাক্সপ্রো ২০ মিগ্রা (Maxpro 20mg)", "১১০ টি বিক্রি", "৳ ৮৮০.০০"),
                            Triple("সারজেল ২০ মিগ্রা (Sergel 20mg)", "৯৫ টি বিক্রি", "৳ ৬৬৫.০০")
                        ).forEach { (name, qty, rev) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(qty, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(rev, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldGreen40))
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                }
            }

            // Customer Dues Summary Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("কাস্টমারের বাকি রিপোর্ট:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("মোট বাকি কাস্টমার: ${customers.count { it.totalDue > 0 }} জন", style = MaterialTheme.typography.bodySmall)
                }
            }

            items(customers.filter { it.totalDue > 0 }) { cust ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(cust.name, fontWeight = FontWeight.Bold)
                            Text("মোবাইল: ${cust.phone}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text("৳ ${String.format("%.2f", cust.totalDue)}", fontWeight = FontWeight.Bold, color = CrimsonRed)
                    }
                }
            }
        }
    }

    // PDF Report Preview Dialog
    if (showPdfReportPreview) {
        val pdfText = """
            ==============================================
                    ${viewModel.shopName.value}
                    ${viewModel.shopAddress.value}
                    মোবাইল: ${viewModel.shopPhone.value}
            ==============================================
            রিপোর্ট প্রকার: বিক্রয় ও লাভ-লোকসান বিবরণী
            তারিখ: আজকের রিপোর্ট
            ----------------------------------------------
            মোট বিক্রি (Sales):          ৳ ${String.format("%.2f", totalSales)}
            আনুমানিক ওষুধের কেনা দাম:  ৳ ${String.format("%.2f", totalCostOfGoods)}
            দোকানের মোট পরিচালনা খরচ:   ৳ ${String.format("%.2f", totalExpenses)}
            ----------------------------------------------
            নিট আনুমানিক লাভ (Net Profit): ৳ ${String.format("%.2f", netProfit)}
            ----------------------------------------------
            মোট বাজার কাস্টমার বাকি:      ৳ ${String.format("%.2f", totalDues)}
            ==============================================
            ধন্যবাদ! জেনারেটকৃত তারিখ: ২০২৬
        """.trimIndent()

        AlertDialog(
            onDismissRequest = { showPdfReportPreview = false },
            title = { Text("PDF / প্রিন্ট প্রিভিউ") },
            text = {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = pdfText,
                        modifier = Modifier.padding(12.dp),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "ফার্মেসি বিক্রয় ও বাকি রিপোর্ট")
                            putExtra(Intent.EXTRA_TEXT, pdfText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "রিপোর্ট শেয়ার করুন"))
                        showPdfReportPreview = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen40)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("রিপোর্ট শেয়ার / প্রিন্ট")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPdfReportPreview = false }) { Text("বন্ধ করুন") }
            }
        )
    }
}

@Composable
fun ReportMiniStat(title: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = color))
    }
}
