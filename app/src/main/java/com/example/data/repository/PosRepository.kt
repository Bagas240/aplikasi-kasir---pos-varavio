package com.example.data.repository

import com.example.data.cache.ProductDataCache
import com.example.data.local.PosDao
import com.example.data.model.CartItem
import com.example.data.model.Customer
import com.example.data.model.CustomerDebt
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.PurchaseOrder
import com.example.data.model.Shift
import com.example.data.model.ShiftSchedule
import com.example.data.model.StaffUser
import com.example.data.model.StockAdjustment
import com.example.data.model.TransactionLog
import com.example.data.model.UserRole
import com.example.util.BarcodeItemMatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import org.json.JSONObject

class PosRepository(
    private val posDao: PosDao,
    val productCache: ProductDataCache = ProductDataCache()
) {

    val transactionLogRepository = TransactionLogRepository(posDao)

    val allProducts: Flow<List<Product>> = posDao.getAllProducts()
    val completedOrders: Flow<List<OrderEntity>> = posDao.getCompletedOrders()
    val draftOrders: Flow<List<OrderEntity>> = posDao.getDraftOrders()
    val allCustomers: Flow<List<Customer>> = posDao.getAllCustomers()
    val allDebts: Flow<List<CustomerDebt>> = posDao.getAllDebts()
    val allAdjustments: Flow<List<StockAdjustment>> = posDao.getAllAdjustments()
    val allPurchaseOrders: Flow<List<PurchaseOrder>> = posDao.getAllPurchaseOrders()
    val allShifts: Flow<List<Shift>> = posDao.getAllShifts()
    val allStaffUsers: Flow<List<StaffUser>> = posDao.getAllStaffUsers()
    val allShiftSchedules: Flow<List<ShiftSchedule>> = posDao.getAllShiftSchedules()
    val allTransactionLogs: Flow<List<TransactionLog>> = transactionLogRepository.allLogs
    val recentSales: Flow<List<TransactionLog>> = transactionLogRepository.recentSales

    suspend fun seedInitialDataIfNeeded() {
        // Remove demo products and demo orders to ensure a clean slate for production
        val demoSkus = listOf(
            "BV-KOP-001", "BV-MAT-002", "BK-CRO-001", "FD-ROT-001",
            "FD-MIE-002", "BV-AIR-003", "SN-KRP-001", "RT-KOS-001"
        )
        posDao.deleteProductsBySkus(demoSkus)

        // Seed Staff Users if empty
        val existingStaff = posDao.getAllStaffUsers().firstOrNull()
        if (existingStaff.isNullOrEmpty()) {
            posDao.insertStaffUsers(
                listOf(
                    StaffUser(id = "U-001", name = "Bagas (Owner)", role = UserRole.OWNER, pin = "1234", phone = "08123456789"),
                    StaffUser(id = "U-002", name = "Dian (Manajer)", role = UserRole.MANAGER, pin = "2222", phone = "08129876543"),
                    StaffUser(id = "U-003", name = "Rian (Kasir 1)", role = UserRole.CASHIER, pin = "0000", phone = "08134567890"),
                    StaffUser(id = "U-004", name = "Siti (Kasir 2)", role = UserRole.CASHIER, pin = "1111", phone = "08135678901")
                )
            )
        }

        // Seed Shift Schedules if empty
        val existingSchedules = posDao.getAllShiftSchedules().firstOrNull()
        if (existingSchedules.isNullOrEmpty()) {
            posDao.insertShiftSchedules(
                listOf(
                    ShiftSchedule(name = "Shift 1 (Pagi)", startTime = "07:00", endTime = "15:00", notes = "Shift pagi operasional utama"),
                    ShiftSchedule(name = "Shift 2 (Sore)", startTime = "15:00", endTime = "23:00", notes = "Shift sore hingga malam"),
                    ShiftSchedule(name = "Shift 3 (Malam)", startTime = "23:00", endTime = "07:00", notes = "Shift malam / 24 jam")
                )
            )
        }
    }

    suspend fun getProductById(id: Long): Product? {
        productCache.getById(id)?.let { return it }
        val product = posDao.getProductById(id)
        if (product != null) {
            productCache.put(product)
        }
        return product
    }

    suspend fun findProductByBarcodeOrSku(query: String): Product? {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return null

        // 1. Check LruCache first (Instant O(1) in-memory lookup, 0 disk I/O)
        productCache.getByBarcodeOrSku(trimmed)?.let { return it }

        // 2. Direct DAO lookup
        val direct = posDao.getProductByBarcodeOrSku(trimmed)
        if (direct != null) {
            productCache.put(direct)
            return direct
        }

        // 3. Fallback: search with cross-format and normalized barcode matching
        val allProductsList = posDao.getAllProducts().firstOrNull() ?: emptyList()
        val matched = BarcodeItemMatcher.matchProduct(trimmed, allProductsList)
        if (matched != null) {
            productCache.put(matched)
        }
        return matched
    }

    suspend fun findProductByBarcode(barcode: String): Product? {
        val trimmed = barcode.trim()
        if (trimmed.isEmpty()) return null
        productCache.getByBarcodeOrSku(trimmed)?.let { return it }
        val product = posDao.getProductByBarcode(trimmed)
        if (product != null) {
            productCache.put(product)
        }
        return product
    }

    fun observeProductByBarcodeOrSku(query: String): Flow<Product?> {
        return posDao.observeProductByBarcodeOrSku(query.trim())
    }

    suspend fun insertProduct(product: Product): Long {
        val id = posDao.insertProduct(product)
        val productWithId = if (product.id == 0L) product.copy(id = id) else product
        productCache.put(productWithId)
        return id
    }

    suspend fun updateProduct(product: Product) {
        posDao.updateProduct(product)
        productCache.put(product)
    }

    suspend fun deleteProduct(product: Product) {
        posDao.deleteProduct(product)
        productCache.remove(product)
    }

    suspend fun updateStock(productId: Long, newStock: Int) {
        posDao.updateStock(productId, newStock)
        productCache.updateStock(productId, newStock)
    }

    suspend fun checkoutOrder(
        order: OrderEntity,
        cartItems: List<CartItem>,
        customer: Customer? = null,
        redeemedPoints: Int = 0
    ) {
        val currentShift = posDao.getCurrentOpenShift()
        val shiftTitle = currentShift?.let { "${it.shiftScheduleName} (${it.shiftScheduleTime})" } ?: ""
        val finalOrder = order.copy(
            shiftId = if (order.shiftId != 0L) order.shiftId else (currentShift?.id ?: 0L),
            shiftName = if (order.shiftName.isNotBlank()) order.shiftName else shiftTitle,
            cashierRole = if (order.cashierRole.isNotBlank()) order.cashierRole else (currentShift?.cashierRole ?: "Kasir")
        )

        // 1. Insert Order
        posDao.insertOrder(finalOrder)

        // 2. Real-time Stock Deduction
        for (item in cartItems) {
            val currentProduct = getProductById(item.product.id)
            if (currentProduct != null) {
                val newStock = (currentProduct.stock - item.quantity).coerceAtLeast(0)
                updateStock(currentProduct.id, newStock)
            }
        }

        // 3. Update Customer Points & Lifetime spend
        if (customer != null) {
            val earnedPoints = (finalOrder.grandTotal / 1000.0).toInt()
            val updatedPoints = (customer.points - redeemedPoints + earnedPoints).coerceAtLeast(0)
            val updatedSpend = customer.totalSpend + finalOrder.grandTotal
            val updatedDebt = if (finalOrder.paymentMethod == PaymentMethod.DEBT.name) {
                customer.debtBalance + finalOrder.grandTotal
            } else customer.debtBalance

            posDao.updateCustomer(
                customer.copy(
                    points = updatedPoints,
                    totalSpend = updatedSpend,
                    debtBalance = updatedDebt,
                    updatedAt = System.currentTimeMillis()
                )
            )

            if (finalOrder.paymentMethod == PaymentMethod.DEBT.name) {
                posDao.insertDebt(
                    CustomerDebt(
                        customerId = customer.id,
                        customerName = customer.name,
                        orderId = finalOrder.orderId,
                        amount = finalOrder.grandTotal,
                        dueDate = System.currentTimeMillis() + (7L * 24 * 3600 * 1000), // 7 days
                        notes = "Belanja POS #${finalOrder.orderId}"
                    )
                )
            }
        }

        // 4. Update active shift cash/digital sales
        if (currentShift != null) {
            var newCashSales = currentShift.cashSales
            var newDigitalSales = currentShift.digitalSales
            var newExpectedCash = currentShift.expectedCash

            if (finalOrder.paymentMethod == PaymentMethod.CASH.name) {
                newCashSales += finalOrder.grandTotal
                newExpectedCash += finalOrder.grandTotal
            } else if (finalOrder.paymentMethod == PaymentMethod.SPLIT.name) {
                newCashSales += finalOrder.splitAmount1
                newExpectedCash += finalOrder.splitAmount1
                newDigitalSales += finalOrder.splitAmount2
            } else if (finalOrder.paymentMethod != PaymentMethod.DEBT.name) {
                newDigitalSales += finalOrder.grandTotal
            }

            posDao.updateShift(
                currentShift.copy(
                    cashSales = newCashSales,
                    digitalSales = newDigitalSales,
                    expectedCash = newExpectedCash
                )
            )
        }

        // 5. Create Transaction Log for Recent Sales and audit trail
        val itemsSummary = cartItems.joinToString(", ") { "${it.displayName} x${it.quantity}" }
        val totalItems = cartItems.sumOf { it.quantity }
        transactionLogRepository.logOrder(
            order = finalOrder,
            itemsSummary = itemsSummary,
            totalItems = totalItems,
            notes = finalOrder.orderNote
        )
    }

    suspend fun saveDraftOrder(draftOrder: OrderEntity) {
        posDao.insertOrder(draftOrder)
    }

    suspend fun deleteDraftOrder(order: OrderEntity) {
        posDao.deleteOrder(order)
    }

    suspend fun adjustStock(
        productId: Long,
        productName: String,
        type: String,
        qtyChange: Int,
        reason: String,
        staffName: String
    ) {
        val prod = getProductById(productId) ?: return
        val prevStock = prod.stock
        val newStock = (prevStock + qtyChange).coerceAtLeast(0)
        updateStock(productId, newStock)

        posDao.insertAdjustment(
            StockAdjustment(
                productId = productId,
                productName = productName,
                type = type,
                qtyChange = qtyChange,
                previousStock = prevStock,
                newStock = newStock,
                reason = reason,
                staffName = staffName
            )
        )
    }

    suspend fun insertCustomer(customer: Customer) = posDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = posDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = posDao.deleteCustomer(customer)

    suspend fun payCustomerDebt(debt: CustomerDebt, customer: Customer) {
        posDao.updateDebt(debt.copy(status = "PAID"))
        val remainingDebt = (customer.debtBalance - debt.amount).coerceAtLeast(0.0)
        posDao.updateCustomer(customer.copy(debtBalance = remainingDebt))
    }

    suspend fun insertPurchaseOrder(po: PurchaseOrder) = posDao.insertPurchaseOrder(po)
    suspend fun markPurchaseOrderReceived(po: PurchaseOrder) {
        posDao.updatePurchaseOrder(po.copy(status = "RECEIVED"))
    }

    suspend fun getCurrentShift(): Shift? = posDao.getCurrentOpenShift()

    suspend fun openShift(
        cashierName: String,
        startingCash: Double,
        shiftScheduleName: String = "Shift 1 (Pagi)",
        shiftScheduleTime: String = "07:00 - 15:00",
        cashierId: String = "",
        cashierRole: String = "Kasir"
    ): Long {
        val shift = Shift(
            shiftScheduleName = shiftScheduleName,
            shiftScheduleTime = shiftScheduleTime,
            cashierName = cashierName,
            cashierId = cashierId,
            cashierRole = cashierRole,
            startingCash = startingCash,
            expectedCash = startingCash,
            status = "OPEN"
        )
        return posDao.insertShift(shift)
    }

    suspend fun closeShift(shift: Shift, actualCash: Double, notes: String) {
        val diff = actualCash - shift.expectedCash
        val updated = shift.copy(
            endTime = System.currentTimeMillis(),
            actualCash = actualCash,
            difference = diff,
            status = "CLOSED",
            notes = notes
        )
        posDao.updateShift(updated)
    }

    suspend fun recordCashInOut(shiftId: Long, amount: Double, isCashIn: Boolean, reason: String) {
        val shift = posDao.getCurrentOpenShift() ?: return
        val newCashIn = if (isCashIn) shift.cashIn + amount else shift.cashIn
        val newCashOut = if (!isCashIn) shift.cashOut + amount else shift.cashOut
        val newExpected = if (isCashIn) shift.expectedCash + amount else shift.expectedCash - amount

        posDao.updateShift(
            shift.copy(
                cashIn = newCashIn,
                cashOut = newCashOut,
                expectedCash = newExpected,
                notes = "${shift.notes}\n[${if (isCashIn) "Kas Masuk" else "Kas Keluar"}] Rp $amount: $reason".trim()
            )
        )
    }

    suspend fun insertStaffUser(user: StaffUser) = posDao.insertStaffUser(user)
    suspend fun updateStaffUser(user: StaffUser) = posDao.updateStaffUser(user)
    suspend fun deleteStaffUser(user: StaffUser) = posDao.deleteStaffUser(user)
    suspend fun getStaffUserByPin(pin: String): StaffUser? = posDao.getStaffUserByPin(pin)

    suspend fun insertShiftSchedule(schedule: ShiftSchedule): Long = posDao.insertShiftSchedule(schedule)
    suspend fun updateShiftSchedule(schedule: ShiftSchedule) = posDao.updateShiftSchedule(schedule)
    suspend fun deleteShiftSchedule(schedule: ShiftSchedule) = posDao.deleteShiftSchedule(schedule)

    fun getTransactionsByCashier(cashierName: String): Flow<List<TransactionLog>> = posDao.getTransactionsByCashier(cashierName)

    // Helper to serialize items to JSON
    companion object {
        fun serializeItems(items: List<CartItem>): String {
            val arr = JSONArray()
            for (item in items) {
                val obj = JSONObject().apply {
                    put("productId", item.product.id)
                    put("name", item.displayName)
                    put("sku", item.displaySku)
                    put("quantity", item.quantity)
                    put("unitPrice", item.unitPrice)
                    put("discountAmount", item.discountAmount)
                    put("totalPrice", item.totalPrice)
                    put("note", item.itemNote)
                }
                arr.put(obj)
            }
            return arr.toString()
        }

        fun deserializeItems(json: String, products: List<Product>): List<CartItem> {
            val result = mutableListOf<CartItem>()
            try {
                val arr = JSONArray(json)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val prodId = obj.optLong("productId", 0L)
                    val prod = products.find { it.id == prodId } ?: Product(
                        id = prodId,
                        name = obj.optString("name", "Product"),
                        sku = obj.optString("sku", ""),
                        barcode = "",
                        category = "General",
                        buyPrice = 0.0,
                        sellPrice = obj.optDouble("unitPrice", 0.0),
                        stock = 100
                    )
                    val qty = obj.optInt("quantity", 1)
                    val note = obj.optString("note", "")
                    result.add(CartItem(product = prod, quantity = qty, itemNote = note))
                }
            } catch (e: Exception) {
                // ignore
            }
            return result
        }
    }
}
