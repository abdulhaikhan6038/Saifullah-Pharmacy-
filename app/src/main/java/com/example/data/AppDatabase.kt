package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.*
import com.example.data.entity.*

@Database(
    entities = [
        CustomerEntity::class,
        CustomerTransactionEntity::class,
        MedicineEntity::class,
        SaleInvoiceEntity::class,
        SaleItemEntity::class,
        CashBoxEntity::class,
        CashTransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun customerTransactionDao(): CustomerTransactionDao
    abstract fun medicineDao(): MedicineDao
    abstract fun saleInvoiceDao(): SaleInvoiceDao
    abstract fun cashBoxDao(): CashBoxDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pharmacy_tali_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
