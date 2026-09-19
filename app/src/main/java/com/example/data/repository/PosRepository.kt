package com.example.data.repository

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
import com.example.data.model.StockAdjustment
import com.example.data.model.TransactionLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import org.json.JSONObject

class PosRepository(private val posDao: PosDao) {

    val transactionLogRepository = TransactionLogRepository(posDao)

    val allProducts: Flow<List<Product>> = posDao.getAllProducts()
    val completedOrders: Flow<List<OrderEntity>> = posDao.getCompletedOrders()
    val draftOrders: Flow<List<OrderEntity>> = posDao.getDraftOrders()
    val allCustomers: Flow<List<Customer>> = posDao.getAllCustomers()
    val allDebts: Flow<List<CustomerDebt>> = posDao.getAllDebts()
    val allAdjustments: Flow<List<StockAdjustment>> = posDao.getAllAdjustments()
    val allPurchaseOrders: Flow<List<PurchaseOrder>> = posDao.getAllPurchaseOrders()
    val allShifts: Flow<List<Shift>> = posDao.getAllShifts()
    val allTransactionLogs: Flow<List<TransactionLog>> = transactionLogRepository.allLogs
    val recentSales: Flow<List<TransactionLog>> = transactionLogRepository.recentSales

    suspend fun seedInitialDataIfNeeded() {
        val existing = posDao.getAllProducts().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val sampleProducts = listOf(
                Product(
                    name = "Kopi Susu Gula Aren",
                    sku = "BV-KOP-001",
                    barcode = "899275321001",
                    category = "Beverages",
                    unit = "Cup",
                    buyPrice = 8000.0,
                    sellPrice = 18000.0,
                    stock = 45,
                    minStockAlert = 10,
                    description = "Espresso robusta & gula aren murni dengan susu segar",
                    hasWholesale = true,
                    wholesaleMinQty = 5,
                    wholesalePrice = 15000.0,
                    iconColor = 0xFF78350F
                ),
                Product(
                    name = "Matcha Latte Premium",
                    sku = "BV-MAT-002",
                    barcode = "899275321002",
                    category = "Beverages",
                    unit = "Cup",
                    buyPrice = 12000.0,
                    sellPrice = 24000.0,
                    stock = 28,
                    minStockAlert = 8,
                    description = "Uji matcha jepang dipadu steamed milk lembut",
                    hasWholesale = false,
                    iconColor = 0xFF065F46
                ),
                Product(
                    name = "Croissant Butter France",
                    sku = "BK-CRO-001",
                    barcode = "899275321003",
                    category = "Bakery",
                    unit = "Pcs",
                    buyPrice = 11000.0,
                    sellPrice = 22000.0,
                    stock = 14,
                    minStockAlert = 5,
                    description = "Flaky crispy golden croissant dengan pure French butter",
                    hasWholesale = true,
                    wholesaleMinQty = 4,
                    wholesalePrice = 19000.0,
                    iconColor = 0xFFD97706
                ),
                Product(
                    name = "Roti Bakar Cokelat Keju",
                    sku = "FD-ROT-001",
                    barcode = "899275321004",
                    category = "Food",
                    unit = "Pcs",
                    buyPrice = 9000.0,
                    sellPrice = 20000.0,
                    stock = 25,
                    minStockAlert = 5,
                    description = "Roti gandum bakar dengan meises cokelat & keju cheddar melimpah",
                    iconColor = 0xFFB45309
                ),
                Product(
                    name = "Mie Goreng Spesial Telur",
                    sku = "FD-MIE-002",
                    barcode = "899275321005",
                    category = "Food",
                    unit = "Pcs",
                    buyPrice = 8500.0,
                    sellPrice = 18000.0,
                    stock = 35,
                    minStockAlert = 10,
                    description = "Mie goreng bumbu racikan khas dengan telur mata sapi & acar",
                    iconColor = 0xFFEA580C
                ),
                Product(
                    name = "Air Mineral 600ml",
                    sku = "BV-AIR-003",
                    barcode = "899275321006",
                    category = "Beverages",
                    unit = "Botol",
                    buyPrice = 2500.0,
                    sellPrice = 5000.0,
                    stock = 3, // LOW STOCK ALERT
                    minStockAlert = 10,
                    description = "Air mineral pegunungan segar 600ml",
                    iconColor = 0xFF0284C7
                ),
                Product(
                    name = "Keripik Singkong Balado",
                    sku = "SN-KRP-001",
                    barcode = "899275321007",
                    category = "Snacks",
                    unit = "Pack",
                    buyPrice = 7000.0,
                    sellPrice = 14000.0,
                    stock = 19,
                    minStockAlert = 5,
                    description = "Keripik renyah rasa balado manis gurih pedas",
                    iconColor = 0xFFDC2626
                ),
                Product(
                    name = "Kaos Polos Cotton 30s",
                    sku = "RT-KOS-001",
                    barcode = "899275321008",
                    category = "Retail",
                    unit = "Pcs",
                    buyPrice = 35000.0,
                    sellPrice = 65000.0,
                    stock = 12,
                    minStockAlert = 5,
                    description = "Kaos oblong 100% cotton combed nyaman adem",
                    iconColor = 0xFF4338CA
                )
            )
            posDao.insertProducts(sampleProducts)

            val sampleCustomers = listOf(
                Customer(
                    name = "Budi Pratama",
                    phone = "081298765432",
                    email = "budi.pratama@gmail.com",
                    address = "Jl. Menteng Asri No. 12",
                    points = 240,
                    debtBalance = 0.0,
                    totalSpend = 480000.0
                ),
                Customer(
                    name = "Siti Rahma",
                    phone = "081311223344",
                    email = "siti.rahma@yahoo.com",
                    address = "Jl. Kebon Jeruk No. 5",
                    points = 180,
                    debtBalance = 45000.0,
                    totalSpend = 620000.0
                ),
                Customer(
                    name = "Hendro Wijaya",
                    phone = "081744556677",
                    email = "hendro.w@gmail.com",
                    address = "Komplek Harmoni No. 8",
                    points = 550,
                    debtBalance = 0.0,
                    totalSpend = 1250000.0
                )
            )
            posDao.insertCustomers(sampleCustomers)

            // Seed initial shift
            val activeShift = Shift(
                cashierName = "Kasir Utama",
                startTime = System.currentTimeMillis() - (3 * 3600 * 1000),
                startingCash = 200000.0,
                cashSales = 133750.0,
                digitalSales = 0.0,
                cashIn = 0.0,
                cashOut = 0.0,
                expectedCash = 333750.0,
                status = "OPEN"
            )
            posDao.insertShift(activeShift)

            // Seed initial completed orders and transaction logs for Recent Sales review
            val initialOrderId = "ORD-20260919-001"
            val sampleOrder1 = OrderEntity(
                orderId = initialOrderId,
                cashierName = "Kasir Utama",
                customerName = "Budi Pratama",
                subtotal = 60000.0,
                taxAmount = 6600.0,
                discountTotal = 0.0,
                serviceAmount = 0.0,
                grandTotal = 66600.0,
                paymentMethod = PaymentMethod.CASH.name,
                cashReceived = 70000.0,
                changeGiven = 3400.0,
                status = OrderStatus.COMPLETED.name,
                orderNote = "Takeaway",
                itemsJson = """[{"id":1,"name":"Kopi Susu Gula Aren","price":20000.0,"qty":2},{"id":2,"name":"Croissant Butter","price":20000.0,"qty":1}]"""
            )
            posDao.insertOrder(sampleOrder1)
            transactionLogRepository.logOrder(
                order = sampleOrder1,
                itemsSummary = "Kopi Susu Gula Aren x2, Croissant Butter x1",
                totalItems = 3,
                notes = "Takeaway"
            )

            val initialOrderId2 = "ORD-20260919-002"
            val sampleOrder2 = OrderEntity(
                orderId = initialOrderId2,
                cashierName = "Kasir Utama",
                customerName = "Siti Rahma",
                subtotal = 65000.0,
                taxAmount = 7150.0,
                discountTotal = 5000.0,
                serviceAmount = 0.0,
                grandTotal = 67150.0,
                paymentMethod = PaymentMethod.QRIS.name,
                cashReceived = 67150.0,
                changeGiven = 0.0,
                status = OrderStatus.COMPLETED.name,
                orderNote = "Dine-in",
                itemsJson = """[{"id":8,"name":"Kaos Polos Cotton Combed 30s","price":65000.0,"qty":1}]"""
            )
            posDao.insertOrder(sampleOrder2)
            transactionLogRepository.logOrder(
                order = sampleOrder2,
                itemsSummary = "Kaos Polos Cotton Combed 30s x1",
                totalItems = 1,
                notes = "Dine-in"
            )
        }
    }

    suspend fun findProductByBarcodeOrSku(query: String): Product? {
        val trimmed = query.trim()
        return posDao.getProductByBarcodeOrSku(trimmed)
    }

    suspend fun findProductByBarcode(barcode: String): Product? {
        val trimmed = barcode.trim()
        return posDao.getProductByBarcode(trimmed)
    }

    fun observeProductByBarcodeOrSku(query: String): Flow<Product?> {
        return posDao.observeProductByBarcodeOrSku(query.trim())
    }

    suspend fun insertProduct(product: Product): Long = posDao.insertProduct(product)
    suspend fun updateProduct(product: Product) = posDao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = posDao.deleteProduct(product)

    suspend fun checkoutOrder(
        order: OrderEntity,
        cartItems: List<CartItem>,
        customer: Customer? = null,
        redeemedPoints: Int = 0
    ) {
        // 1. Insert Order
        posDao.insertOrder(order)

        // 2. Real-time Stock Deduction
        for (item in cartItems) {
            val currentProduct = posDao.getProductById(item.product.id)
            if (currentProduct != null) {
                val newStock = (currentProduct.stock - item.quantity).coerceAtLeast(0)
                posDao.updateStock(currentProduct.id, newStock)
            }
        }

        // 3. Update Customer Points & Lifetime spend
        if (customer != null) {
            val earnedPoints = (order.grandTotal / 1000.0).toInt()
            val updatedPoints = (customer.points - redeemedPoints + earnedPoints).coerceAtLeast(0)
            val updatedSpend = customer.totalSpend + order.grandTotal
            val updatedDebt = if (order.paymentMethod == PaymentMethod.DEBT.name) {
                customer.debtBalance + order.grandTotal
            } else customer.debtBalance

            posDao.updateCustomer(
                customer.copy(
                    points = updatedPoints,
                    totalSpend = updatedSpend,
                    debtBalance = updatedDebt,
                    updatedAt = System.currentTimeMillis()
                )
            )

            if (order.paymentMethod == PaymentMethod.DEBT.name) {
                posDao.insertDebt(
                    CustomerDebt(
                        customerId = customer.id,
                        customerName = customer.name,
                        orderId = order.orderId,
                        amount = order.grandTotal,
                        dueDate = System.currentTimeMillis() + (7L * 24 * 3600 * 1000), // 7 days
                        notes = "Belanja POS #${order.orderId}"
                    )
                )
            }
        }

        // 4. Update active shift cash/digital sales
        val currentShift = posDao.getCurrentOpenShift()
        if (currentShift != null) {
            var newCashSales = currentShift.cashSales
            var newDigitalSales = currentShift.digitalSales
            var newExpectedCash = currentShift.expectedCash

            if (order.paymentMethod == PaymentMethod.CASH.name) {
                newCashSales += order.grandTotal
                newExpectedCash += order.grandTotal
            } else if (order.paymentMethod == PaymentMethod.SPLIT.name) {
                newCashSales += order.splitAmount1
                newExpectedCash += order.splitAmount1
                newDigitalSales += order.splitAmount2
            } else if (order.paymentMethod != PaymentMethod.DEBT.name) {
                newDigitalSales += order.grandTotal
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
            order = order,
            itemsSummary = itemsSummary,
            totalItems = totalItems,
            notes = order.orderNote
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
        val prod = posDao.getProductById(productId) ?: return
        val prevStock = prod.stock
        val newStock = (prevStock + qtyChange).coerceAtLeast(0)
        posDao.updateStock(productId, newStock)

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

    suspend fun openShift(cashierName: String, startingCash: Double): Long {
        val shift = Shift(
            cashierName = cashierName,
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
