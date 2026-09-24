package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Customer
import com.example.data.model.CustomerDebt
import com.example.data.model.OrderEntity
import com.example.data.model.Product
import com.example.data.model.PurchaseOrder
import com.example.data.model.Shift
import com.example.data.model.ShiftSchedule
import com.example.data.model.StaffUser
import com.example.data.model.StockAdjustment
import com.example.data.model.TransactionLog

@Database(
    entities = [
        Product::class,
        OrderEntity::class,
        Customer::class,
        CustomerDebt::class,
        StockAdjustment::class,
        PurchaseOrder::class,
        Shift::class,
        TransactionLog::class,
        StaffUser::class,
        ShiftSchedule::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun posDao(): PosDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pos_master_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
