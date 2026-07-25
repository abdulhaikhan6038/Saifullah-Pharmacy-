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
import com.example.data.entity.MedicineEntity
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen40
import com.example.ui.viewmodel.PharmacyViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: PharmacyViewModel,
    modifier: Modifier = Modifier
) {
    val medicines by viewModel.allMedicines.collectAsState()
    val lowStockMeds by viewModel.lowStockMedicines.collectAsState()
    val searchQuery by viewModel.medicineQuery.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Low Stock, 2: Expiring Soon
    var showAddMedicineDialog by remember { mutableStateOf(false) }
    var medicineToEdit by remember { mutableStateOf<MedicineEntity?>(null) }

    val filteredList = when (selectedTab) {
        1 -> lowStockMeds
        2 -> medicines.filter { it.expiryDate.startsWith("2026-08") || it.expiryDate.startsWith("2026-09") }
        else -> medicines
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Top Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ওষুধ ও ইনভেন্টরি",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "মোট ওষুধ: ${medicines.size} টি, স্টক কম: ${lowStockMeds.size} টি",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { showAddMedicineDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen40),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("add_medicine_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "নতুন ওষুধ")
                Spacer(modifier = Modifier.width(4.dp))
                Text("নতুন ওষুধ")
            }
        }

        // Search & Filter Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.medicineQuery.value = it },
            placeholder = { Text("ওষুধের নাম, বারকোড বা ক্যাটাগরি সার্চ...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.medicineQuery.value = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("search_medicine_input")
        )

        // Filter Tabs
        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("সকল ওষুধ (${medicines.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("কম স্টক ")
                        if (lowStockMeds.isNotEmpty()) {
                            Badge(containerColor = CrimsonRed) { Text("${lowStockMeds.size}") }
                        }
                    }
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("মেয়াদি সতর্কবার্তা") }
            )
        }

        // Medicine Cards List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Medication,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "কোন ওষুধ পাওয়া যায়নি",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredList, key = { it.id }) { med ->
                    MedicineCardItem(
                        medicine = med,
                        onEditClick = { medicineToEdit = med },
                        onAddToCart = { viewModel.addToCart(med) }
                    )
                }
            }
        }
    }

    // Add / Edit Medicine Dialog
    if (showAddMedicineDialog || medicineToEdit != null) {
        AddOrEditMedicineDialog(
            initialMedicine = medicineToEdit,
            onDismiss = {
                showAddMedicineDialog = false
                medicineToEdit = null
            },
            onSave = { updatedMed ->
                viewModel.addOrUpdateMedicine(updatedMed)
                showAddMedicineDialog = false
                medicineToEdit = null
            },
            onDelete = { medToDelete ->
                viewModel.deleteMedicine(medToDelete)
                showAddMedicineDialog = false
                medicineToEdit = null
            }
        )
    }
}

@Composable
fun MedicineCardItem(
    medicine: MedicineEntity,
    onEditClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    val isLowStock = medicine.stockQuantity <= medicine.minStockAlert
    val isExpiringSoon = medicine.expiryDate.startsWith("2026-08") || medicine.expiryDate.startsWith("2026-09")

    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("medicine_item_${medicine.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(medicine.category, fontSize = 10.sp) },
                            modifier = Modifier.height(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (medicine.barcode.isNotBlank()) {
                            Text(
                                text = "বারকোড: ${medicine.barcode}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = medicine.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "ব্যাচ: ${if (medicine.batchNumber.isBlank()) "N/A" else medicine.batchNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "মেয়াদ: ${if (medicine.expiryDate.isBlank()) "N/A" else medicine.expiryDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isExpiringSoon) AmberWarning else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isExpiringSoon) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "৳ ${String.format("%.2f", medicine.sellPrice)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen40
                        )
                    )
                    Text(
                        text = "ক্রয়: ৳${String.format("%.2f", medicine.buyPrice)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Stock & Quick Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (isLowStock) CrimsonRed.copy(alpha = 0.15f) else EmeraldGreen40.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "স্টক: ${medicine.stockQuantity} টি",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isLowStock) CrimsonRed else EmeraldGreen40
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (isLowStock) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "⚠️ স্টক কম!",
                            style = MaterialTheme.typography.labelSmall,
                            color = CrimsonRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "সম্পাদনা", modifier = Modifier.size(20.dp))
                    }

                    Button(
                        onClick = onAddToCart,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen40),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        enabled = medicine.stockQuantity > 0,
                        modifier = Modifier.testTag("add_to_cart_btn_${medicine.id}")
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("কার্টে নিন", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditMedicineDialog(
    initialMedicine: MedicineEntity?,
    onDismiss: () -> Unit,
    onSave: (MedicineEntity) -> Unit,
    onDelete: (MedicineEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialMedicine?.name ?: "") }
    var barcode by remember { mutableStateOf(initialMedicine?.barcode ?: "") }
    var batchNumber by remember { mutableStateOf(initialMedicine?.batchNumber ?: "") }
    var expiryDate by remember { mutableStateOf(initialMedicine?.expiryDate ?: "2027-12-31") }
    var category by remember { mutableStateOf(initialMedicine?.category ?: "ট্যাবলেট") }
    var buyPriceText by remember { mutableStateOf(initialMedicine?.buyPrice?.toString() ?: "") }
    var sellPriceText by remember { mutableStateOf(initialMedicine?.sellPrice?.toString() ?: "") }
    var stockText by remember { mutableStateOf(initialMedicine?.stockQuantity?.toString() ?: "50") }
    var minStockAlertText by remember { mutableStateOf(initialMedicine?.minStockAlert?.toString() ?: "10") }

    val categories = listOf("ট্যাবলেট", "ক্যাপসুল", "সিরাপ", "ইনজেকশন", "ড্রপ", "ক্রিম", "অন্যান্য")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialMedicine == null) "নতুন ওষুধ যুক্ত করুন" else "ওষুধ সম্পাদনা করুন") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("ওষুধের নাম *") },
                        modifier = Modifier.fillMaxWidth().testTag("medicine_name_input")
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text("বারকোড / SKU") },
                            modifier = Modifier.weight(1f).testTag("medicine_barcode_input")
                        )
                        IconButton(
                            onClick = { barcode = "890" + (1000..9999).random() },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "জেনারেট")
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = batchNumber,
                            onValueChange = { batchNumber = it },
                            label = { Text("ব্যাচ নম্বর") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = { expiryDate = it },
                            label = { Text("মেয়াদোত্তীর্ণ (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Text("ক্যাটাগরি নির্বাচন:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.take(4).forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 10.sp) }
                            )
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = buyPriceText,
                            onValueChange = { buyPriceText = it },
                            label = { Text("ক্রয়মূল্য (৳)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sellPriceText,
                            onValueChange = { sellPriceText = it },
                            label = { Text("বিক্রয়মূল্য (৳) *") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stockText,
                            onValueChange = { stockText = it },
                            label = { Text("বর্তমান স্টক (টি) *") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = minStockAlertText,
                            onValueChange = { minStockAlertText = it },
                            label = { Text("কম স্টক অ্যালার্ট সীমা") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val newMed = MedicineEntity(
                            id = initialMedicine?.id ?: 0L,
                            name = name,
                            barcode = barcode,
                            batchNumber = batchNumber,
                            expiryDate = expiryDate,
                            category = category,
                            buyPrice = buyPriceText.toDoubleOrNull() ?: 0.0,
                            sellPrice = sellPriceText.toDoubleOrNull() ?: 0.0,
                            stockQuantity = stockText.toIntOrNull() ?: 0,
                            minStockAlert = minStockAlertText.toIntOrNull() ?: 10
                        )
                        onSave(newMed)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen40),
                modifier = Modifier.testTag("save_medicine_button")
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            Row {
                if (initialMedicine != null) {
                    TextButton(
                        onClick = { onDelete(initialMedicine) },
                        colors = ButtonDefaults.textButtonColors(contentColor = CrimsonRed)
                    ) {
                        Text("মুছে ফেলুন")
                    }
                }
                TextButton(onClick = onDismiss) { Text("বাতিল") }
            }
        }
    )
}
