package com.example.data.dao

import androidx.room.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY updatedAt DESC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'")
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("SELECT SUM(totalDue) FROM customers")
    fun getTotalDuesFlow(): Flow<Double?>
}

@Dao
interface CustomerTransactionDao {
    @Query("SELECT * FROM customer_transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getTransactionsForCustomer(customerId: Long): Flow<List<CustomerTransactionEntity>>

    @Query("SELECT * FROM customer_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<CustomerTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: CustomerTransactionEntity): Long
}

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines ORDER BY name ASC")
    fun getAllMedicines(): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE stockQuantity <= minStockAlert ORDER BY stockQuantity ASC")
    fun getLowStockMedicines(): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineById(id: Long): MedicineEntity?

    @Query("SELECT * FROM medicines WHERE barcode = :barcode LIMIT 1")
    suspend fun getMedicineByBarcode(barcode: String): MedicineEntity?

    @Query("SELECT * FROM medicines WHERE name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchMedicines(query: String): Flow<List<MedicineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity): Long

    @Update
    suspend fun updateMedicine(medicine: MedicineEntity)

    @Delete
    suspend fun deleteMedicine(medicine: MedicineEntity)
}

@Dao
interface SaleInvoiceDao {
    @Query("SELECT * FROM sale_invoices ORDER BY timestamp DESC")
    fun getAllInvoices(): Flow<List<SaleInvoiceEntity>>

    @Query("SELECT * FROM sale_invoices WHERE id = :id")
    suspend fun getInvoiceById(id: Long): SaleInvoiceEntity?

    @Query("SELECT * FROM sale_items WHERE invoiceId = :invoiceId")
    suspend fun getItemsForInvoice(invoiceId: Long): List<SaleItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: SaleInvoiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Query("SELECT SUM(totalAmount) FROM sale_invoices WHERE timestamp >= :startTime")
    fun getTotalSalesSince(startTime: Long): Flow<Double?>
}

@Dao
interface CashBoxDao {
    @Query("SELECT * FROM cash_box_daily WHERE dateString = :dateLimit LIMIT 1")
    suspend fun getCashBoxForDate(dateLimit: String): CashBoxEntity?

    @Query("SELECT * FROM cash_box_daily ORDER BY id DESC LIMIT 1")
    fun getLatestCashBoxFlow(): Flow<CashBoxEntity?>

    @Query("SELECT * FROM cash_box_daily ORDER BY id DESC")
    fun getAllCashBoxes(): Flow<List<CashBoxEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCashBox(cashBox: CashBoxEntity): Long

    @Query("SELECT * FROM cash_transactions ORDER BY timestamp DESC")
    fun getAllCashTransactions(): Flow<List<CashTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashTransaction(transaction: CashTransactionEntity): Long
}
