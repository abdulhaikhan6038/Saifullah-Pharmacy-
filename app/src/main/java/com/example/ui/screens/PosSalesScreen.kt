package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CartItem
import com.example.data.entity.CustomerEntity
import com.example.data.entity.MedicineEntity
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen40
import com.example.ui.theme.ForestGreenDeep
import com.example.ui.viewmodel.PharmacyViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosSalesScreen(
    viewModel: PharmacyViewModel,
    onViewInvoice: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val medicines by viewModel.allMedicines.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()
    val cart by viewModel.cartItems.collectAsState()
    val selectedCust by viewModel.selectedCustomer.collectAsState()
    val discountText by viewModel.discountInput.collectAsState()
    val paidText by viewModel.paidInput.collectAsState()
    val paymentMethod by viewModel.paymentMethodInput.collectAsState()

    var barcodeQuery by remember { mutableStateOf("") }
    var showCustomerPickerSheet by remember { mutableStateOf(false) }

    // Computations
    val subtotal = cart.sumOf { it.medicine.sellPrice * it.quantity }
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val netTotal = (subtotal - discount).coerceAtLeast(0.0)
    val paid = paidText.toDoubleOrNull() ?: netTotal
    val dueAmount = (netTotal - paid).coerceAtLeast(0.0)

    // Filter medicines for quick search
    val filteredMeds = if (barcodeQuery.isBlank()) medicines
    else medicines.filter {
        it.name.contains(barcodeQuery, ignoreCase = true) ||
        it.barcode.contains(barcodeQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Top Counter Header
        Text(
            text = "কাউন্টার বিক্রি (POS Billing)",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left Column: Medicine Picker
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
            ) {
                OutlinedTextField(
                    value = barcodeQuery,
                    onValueChange = { barcodeQuery = it },
                    placeholder = { Text("ওষুধ বা বারকোড খুঁজুন...") },
                    leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                    trailingIcon = {
                        if (barcodeQuery.isNotEmpty()) {
                            IconButton(onClick = { barcodeQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag("pos_search_input")
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredMeds, key = { it.id }) { med ->
                        Card(
                            onClick = { viewModel.addToCart(med) },
                            enabled = med.stockQuantity > 0,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pos_med_item_${med.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = med.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "স্টক: ${med.stockQuantity} টি | ৳${med.sellPrice}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (med.stockQuantity <= med.minStockAlert) CrimsonRed else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.addToCart(med) },
                                    enabled = med.stockQuantity > 0
                                ) {
                                    Icon(
                                        Icons.Default.AddCircle,
                                        contentDescription = "Add",
                                        tint = if (med.stockQuantity > 0) EmeraldGreen40 else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Right Column: Cart & Checkout
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    // Cart Header & Customer Picker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "বিক্রয় মেমো (${cart.sumOf { it.quantity }} টি)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        TextButton(
                            onClick = { showCustomerPickerSheet = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = ForestGreenDeep),
                            modifier = Modifier.testTag("select_customer_btn")
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = selectedCust?.name ?: "সাধারণ কাস্টমার",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    HorizontalDivider()

                    // Cart Items List
                    if (cart.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "কার্ট খালি। বাম পাশ থেকে ওষুধ সিলেক্ট করুন।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                        ) {
                            items(cart, key = { it.medicine.id }) { item ->
                                CartRowItem(
                                    cartItem = item,
                                    onQtyChange = { newQty -> viewModel.updateCartQuantity(item.medicine.id, newQty) }
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    // Bill Breakdown
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("মোট সাবটোটাল:")
                            Text("৳ ${String.format("%.2f", subtotal)}", fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ছাড় (Discount):")
                            OutlinedTextField(
                                value = discountText,
                                onValueChange = { viewModel.discountInput.value = it },
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(48.dp),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                singleLine = true
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("সর্বমোট পরিশোধযোগ্য:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("৳ ${String.format("%.2f", netTotal)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldGreen40))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("জমা (Paid):")
                            OutlinedTextField(
                                value = paidText,
                                onValueChange = { viewModel.paidInput.value = it },
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(48.dp),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                singleLine = true
                            )
                        }

                        if (dueAmount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("বাকি থাকবে:", color = CrimsonRed, fontWeight = FontWeight.Bold)
                                Text("৳ ${String.format("%.2f", dueAmount)}", color = CrimsonRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Payment method chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("CASH" to "নগদ", "BKASH" to "বিকাশ", "NAGAD" to "নগদ (অ্যাপ)", "DUE" to "সম্পূর্ণ বাকি").forEach { (code, label) ->
                            FilterChip(
                                selected = paymentMethod == code,
                                onClick = { viewModel.paymentMethodInput.value = code },
                                label = { Text(label, fontSize = 10.sp) }
                            )
                        }
                    }

                    // Checkout Button
                    Button(
                        onClick = {
                            viewModel.checkoutPos { invoiceId ->
                                onViewInvoice(invoiceId)
                            }
                        },
                        enabled = cart.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen40),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("checkout_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("বিক্রয় সম্পন্ন করুন (৳${String.format("%.2f", netTotal)})", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Customer Picker Dialog
    if (showCustomerPickerSheet) {
        AlertDialog(
            onDismissRequest = { showCustomerPickerSheet = false },
            title = { Text("কাস্টমার সিলেক্ট করুন") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            viewModel.selectedCustomer.value = null
                            showCustomerPickerSheet = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("সাধারণ কাস্টমার (ক্যাশ বিক্রি)")
                    }
                    HorizontalDivider()
                    LazyColumn(modifier = Modifier.height(250.dp)) {
                        items(customers) { cust ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectedCustomer.value = cust
                                        showCustomerPickerSheet = false
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(cust.name, fontWeight = FontWeight.Bold)
                                Text("বাকি: ৳${cust.totalDue}", color = if (cust.totalDue > 0) CrimsonRed else EmeraldGreen40)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCustomerPickerSheet = false }) { Text("বাতিল") }
            }
        )
    }
}

@Composable
fun CartRowItem(
    cartItem: CartItem,
    onQtyChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(cartItem.medicine.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1)
            Text("৳${cartItem.medicine.sellPrice} x ${cartItem.quantity} = ৳${String.format("%.2f", cartItem.medicine.sellPrice * cartItem.quantity)}", style = MaterialTheme.typography.bodySmall)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onQtyChange(cartItem.quantity - 1) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Remove, contentDescription = "কমাইন", modifier = Modifier.size(16.dp))
            }
            Text("${cartItem.quantity}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
            IconButton(onClick = { onQtyChange(cartItem.quantity + 1) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Add, contentDescription = "বাড়ান", modifier = Modifier.size(16.dp))
            }
        }
    }
}
