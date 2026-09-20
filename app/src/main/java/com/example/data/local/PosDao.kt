package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface PosDao {
    // PRODUCTS
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE barcode = :code OR sku = :code LIMIT 1")
    suspend fun getProductByBarcodeOrSku(code: String): Product?

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): Product?

    @Query("SELECT * FROM products WHERE barcode = :code OR sku = :code LIMIT 1")
    fun observeProductByBarcodeOrSku(code: String): Flow<Product?>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET stock = :newStock WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Int)

    @Query("DELETE FROM products WHERE sku IN (:skus)")
    suspend fun deleteProductsBySkus(skus: List<String>)

    // ORDERS
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status = 'COMPLETED' ORDER BY timestamp DESC")
    fun getCompletedOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status = 'HELD_DRAFT' ORDER BY timestamp DESC")
    fun getDraftOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderId = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)

    // CUSTOMERS
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<Customer>)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    // DEBTS
    @Query("SELECT * FROM customer_debts ORDER BY createdAt DESC")
    fun getAllDebts(): Flow<List<CustomerDebt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: CustomerDebt): Long

    @Update
    suspend fun updateDebt(debt: CustomerDebt)

    // INVENTORY / ADJUSTMENTS
    @Query("SELECT * FROM stock_adjustments ORDER BY timestamp DESC")
    fun getAllAdjustments(): Flow<List<StockAdjustment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdjustment(adjustment: StockAdjustment): Long

    // PURCHASE ORDERS
    @Query("SELECT * FROM purchase_orders ORDER BY createdAt DESC")
    fun getAllPurchaseOrders(): Flow<List<PurchaseOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseOrder(po: PurchaseOrder): Long

    @Update
    suspend fun updatePurchaseOrder(po: PurchaseOrder)

    // SHIFTS
    @Query("SELECT * FROM shifts ORDER BY startTime DESC")
    fun getAllShifts(): Flow<List<Shift>>

    @Query("SELECT * FROM shifts WHERE status = 'OPEN' ORDER BY startTime DESC LIMIT 1")
    suspend fun getCurrentOpenShift(): Shift?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: Shift): Long

    @Update
    suspend fun updateShift(shift: Shift)

    // TRANSACTION LOGS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionLog(log: TransactionLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionLogs(logs: List<TransactionLog>)

    @Query("SELECT * FROM transaction_logs ORDER BY timestamp DESC")
    fun getAllTransactionLogs(): Flow<List<TransactionLog>>

    @Query("SELECT * FROM transaction_logs WHERE status = 'COMPLETED' ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSales(limit: Int = 50): Flow<List<TransactionLog>>

    @Query("SELECT * FROM transaction_logs WHERE orderId = :orderId LIMIT 1")
    suspend fun getTransactionLogByOrderId(orderId: String): TransactionLog?

    @Query("DELETE FROM transaction_logs WHERE id = :id")
    suspend fun deleteTransactionLog(id: Long)

    // STAFF USERS & CASHIERS
    @Query("SELECT * FROM staff_users ORDER BY role ASC, name ASC")
    fun getAllStaffUsers(): Flow<List<StaffUser>>

    @Query("SELECT * FROM staff_users WHERE id = :id LIMIT 1")
    suspend fun getStaffUserById(id: String): StaffUser?

    @Query("SELECT * FROM staff_users WHERE pin = :pin LIMIT 1")
    suspend fun getStaffUserByPin(pin: String): StaffUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaffUser(user: StaffUser)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaffUsers(users: List<StaffUser>)

    @Update
    suspend fun updateStaffUser(user: StaffUser)

    @Delete
    suspend fun deleteStaffUser(user: StaffUser)

    // SHIFT SCHEDULES (Shift 1, Shift 2, dll)
    @Query("SELECT * FROM shift_schedules ORDER BY id ASC")
    fun getAllShiftSchedules(): Flow<List<ShiftSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShiftSchedule(schedule: ShiftSchedule): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShiftSchedules(schedules: List<ShiftSchedule>)

    @Update
    suspend fun updateShiftSchedule(schedule: ShiftSchedule)

    @Delete
    suspend fun deleteShiftSchedule(schedule: ShiftSchedule)

    // TRANSACTIONS BY CASHIER
    @Query("SELECT * FROM transaction_logs WHERE cashierName = :cashierName ORDER BY timestamp DESC")
    fun getTransactionsByCashier(cashierName: String): Flow<List<TransactionLog>>
}
