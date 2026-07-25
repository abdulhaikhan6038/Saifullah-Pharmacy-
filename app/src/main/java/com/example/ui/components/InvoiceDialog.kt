package com.example.ui.components

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.SaleInvoiceEntity
import com.example.data.entity.SaleItemEntity
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen40
import com.example.ui.viewmodel.PharmacyViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun InvoiceDialog(
    invoiceId: Long,
    viewModel: PharmacyViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val invoices by viewModel.allInvoices.collectAsState()
    val invoice = invoices.find { it.id == invoiceId } ?: return

    var items by remember { mutableStateOf<List<SaleItemEntity>>(emptyList()) }

    LaunchedEffect(invoiceId) {
        // Load invoice items from repository
    }

    val receiptText = """
        ========================================
                 ${viewModel.shopName.value}
                 ${viewModel.shopAddress.value}
                 মোবাইল: ${viewModel.shopPhone.value}
        ========================================
        ইনভয়েস নং: #${invoice.invoiceNumber}
        কাস্টমার: ${invoice.customerName}
        পেমেন্ট পদ্ধতি: ${invoice.paymentMethod}
        ক্যাশিয়ার: ${invoice.employeeName}
        ----------------------------------------
        বিক্রয় মেমো বিস্তারিত
        ----------------------------------------
        মোট সাবটোটাল: ৳ ${String.format("%.2f", invoice.totalAmount)}
        ছাড় (Discount):  ৳ ${String.format("%.2f", invoice.discountAmount)}
        ----------------------------------------
        সর্বমোট প্রদেয়: ৳ ${String.format("%.2f", invoice.netAmount)}
        জমা (Paid):     ৳ ${String.format("%.2f", invoice.paidAmount)}
        বাকি (Due):      ৳ ${String.format("%.2f", invoice.dueAmount)}
        ========================================
               ধন্যবাদ! আবার আসবেন।
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("বিক্রয় রসিদ (Invoice #${invoice.invoiceNumber})")
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = receiptText,
                        modifier = Modifier.padding(12.dp),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, receiptText)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "ইনভয়েস মেমো পাঠান"))
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen40)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("মেমো শেয়ার / ওয়াটসঅ্যাপ")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        }
    )
}
