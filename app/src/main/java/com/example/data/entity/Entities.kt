package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val address: String = "",
    val totalDue: Double = 0.0,
    val totalPaid: Double = 0.0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customer_transactions")
data class CustomerTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val amount: Double,
    val type: String, // "DUE" (বাকি), "PAYMENT" (জমা), "CASH_SALE" (নগদ বিক্রি)
    val note: String = "",
    val paymentMethod: String = "CASH", // CASH, BKASH, NAGAD, BANK
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val barcode: String = "",
    val batchNumber: String = "",
    val expiryDate: String = "", // e.g. "2026-12-31"
    val buyPrice: Double = 0.0,
    val sellPrice: Double = 0.0,
    val stockQuantity: Int = 0,
    val minStockAlert: Int = 10,
    val category: String = "ট্যাবলেট", // ট্যাবলেট, সিরাপ, ইনজেকশন, ক্যাপসুল, ড্রপ, ক্রিম, অন্যান্য
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sale_invoices")
data class SaleInvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val customerName: String = "সাধারণ কাস্টমার",
    val customerPhone: String = "",
    val totalAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val netAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val paymentMethod: String = "CASH", // CASH, BKASH, NAGAD, BANK, DUE
    val employeeName: String = "এডমিন",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sale_items")
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val medicineId: Long,
    val medicineName: String,
    val batchNumber: String = "",
    val quantity: Int,
    val unitPrice: Double,
    val unitBuyPrice: Double = 0.0,
    val subtotal: Double
)

@Entity(tableName = "cash_box_daily")
data class CashBoxEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String, // e.g. "YYYY-MM-DD"
    val openingBalance: Double = 0.0,
    val cashSales: Double = 0.0,
    val cashExpenses: Double = 0.0,
    val cashIn: Double = 0.0,
    val cashOut: Double = 0.0,
    val closingBalanceCalculated: Double = 0.0,
    val closingBalanceActual: Double? = null,
    val shortOrOverAmount: Double? = null,
    val isClosed: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cash_transactions")
data class CashTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "EXPENSE" (খরচ), "CASH_IN" (জমা), "CASH_OUT" (উত্তোলন), "SALE" (নগদ বিক্রি), "DUE_COLLECTION" (বাকি আদায়)
    val title: String,
    val amount: Double,
    val category: String = "সাধারণ",
    val paymentMethod: String = "CASH",
    val timestamp: Long = System.currentTimeMillis()
)
