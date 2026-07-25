package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.PharmacyRepository
import com.example.ui.components.InvoiceDialog
import com.example.ui.screens.*
import com.example.ui.theme.EmeraldGreen40
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PharmacyViewModel
import com.example.ui.viewmodel.PharmacyViewModelFactory

sealed class NavigationTab(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Ledger : NavigationTab("ledger", "টালিখাতা", Icons.Default.MenuBook)
    object Pos : NavigationTab("pos", "বিক্রয়", Icons.Default.PointOfSale)
    object Inventory : NavigationTab("inventory", "ওষুধ", Icons.Default.Medication)
    object CashBox : NavigationTab("cashbox", "ক্যাশ বাক্স", Icons.Default.AccountBalanceWallet)
    object Reports : NavigationTab("reports", "রিপোর্ট", Icons.Default.Assessment)
    object Settings : NavigationTab("settings", "সেটিংস", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                PharmacyAppMain()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacyAppMain() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember {
        PharmacyRepository(
            customerDao = database.customerDao(),
            customerTransactionDao = database.customerTransactionDao(),
            medicineDao = database.medicineDao(),
            saleInvoiceDao = database.saleInvoiceDao(),
            cashBoxDao = database.cashBoxDao()
        )
    }
    val viewModel: PharmacyViewModel = viewModel(
        factory = PharmacyViewModelFactory(repository)
    )

    var currentTab by remember { mutableStateOf<NavigationTab>(NavigationTab.Ledger) }
    var viewingInvoiceId by remember { mutableStateOf<Long?>(null) }

    val tabs = listOf(
        NavigationTab.Ledger,
        NavigationTab.Pos,
        NavigationTab.Inventory,
        NavigationTab.CashBox,
        NavigationTab.Reports,
        NavigationTab.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        modifier = Modifier.testTag("nav_tab_${tab.route}")
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavigationTab.Ledger -> CustomerLedgerScreen(viewModel = viewModel)
                NavigationTab.Pos -> PosSalesScreen(
                    viewModel = viewModel,
                    onViewInvoice = { invoiceId -> viewingInvoiceId = invoiceId }
                )
                NavigationTab.Inventory -> InventoryScreen(viewModel = viewModel)
                NavigationTab.CashBox -> CashBoxScreen(viewModel = viewModel)
                NavigationTab.Reports -> ReportsScreen(viewModel = viewModel)
                NavigationTab.Settings -> SettingsScreen(viewModel = viewModel)
            }
        }

        viewingInvoiceId?.let { invId ->
            InvoiceDialog(
                invoiceId = invId,
                viewModel = viewModel,
                onDismiss = { viewingInvoiceId = null }
            )
        }
    }
}
