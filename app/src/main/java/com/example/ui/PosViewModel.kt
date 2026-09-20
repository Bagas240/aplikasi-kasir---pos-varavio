package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AuthPreferences
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
import com.example.data.model.StoreProfile
import com.example.data.model.TransactionLog
import com.example.data.model.UserRole
import com.example.data.repository.PosRepository
import com.example.util.BarcodeFormat
import com.example.util.SoundHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class PosTab(val title: String) {
    CASHIER("Kasir POS"),
    BARCODE_ENGINE("Barcode & Cetak"),
    CATALOG("Katalog Produk"),
    INVENTORY("Stok & Opname"),
    CRM("Pelanggan & Utang"),
    SHIFT("Shift Kasir"),
    ANALYTICS("Laporan & Toko")
}

class PosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PosRepository
    init {
        val database = AppDatabase.getDatabase(application)
        repository = PosRepository(database.posDao())
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
        viewModelScope.launch {
            repository.allStaffUsers.collect { users ->
                if (users.isNotEmpty()) {
                    val matching = users.find { it.id == _currentUser.value.id }
                    if (matching != null) {
                        _currentUser.value = matching
                    } else if (_currentUser.value.id == "U-001") {
                        _currentUser.value = users.first()
                    }
                }
            }
        }
    }

    // AUTH / STAFF
    private val _currentUser = MutableStateFlow(
        StaffUser(id = "U-001", name = "Bagas (Owner)", role = UserRole.OWNER, pin = "1234")
    )
    val currentUser: StateFlow<StaffUser> = _currentUser.asStateFlow()

    // NAVIGATION
    private val _currentTab = MutableStateFlow(PosTab.CASHIER)
    val currentTab: StateFlow<PosTab> = _currentTab.asStateFlow()
    fun setTab(tab: PosTab) { _currentTab.value = tab }

    // DATA FROM REPOSITORY
    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedOrders: StateFlow<List<OrderEntity>> = repository.completedOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val draftOrders: StateFlow<List<OrderEntity>> = repository.draftOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val debts: StateFlow<List<CustomerDebt>> = repository.allDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adjustments: StateFlow<List<StockAdjustment>> = repository.allAdjustments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchaseOrders: StateFlow<List<PurchaseOrder>> = repository.allPurchaseOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shifts: StateFlow<List<Shift>> = repository.allShifts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val staffUsers: StateFlow<List<StaffUser>> = repository.allStaffUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shiftSchedules: StateFlow<List<ShiftSchedule>> = repository.allShiftSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactionLogs: StateFlow<List<TransactionLog>> = repository.allTransactionLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSales: StateFlow<List<TransactionLog>> = repository.recentSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // STORE PROFILE
    private val _storeProfile = MutableStateFlow(
        run {
            val prefs = AuthPreferences(application)
            StoreProfile(
                storeName = prefs.storeName,
                address = prefs.storeAddress,
                phone = prefs.storePhone,
                logoUri = prefs.storeLogoUri,
                qrisImageUri = prefs.storeQrisImageUri
            )
        }
    )
    val storeProfile: StateFlow<StoreProfile> = _storeProfile.asStateFlow()

    fun updateStoreProfile(profile: StoreProfile) {
        _storeProfile.value = profile
        val authPrefs = AuthPreferences(getApplication())
        authPrefs.saveStoreProfile(
            name = profile.storeName,
            address = profile.address,
            phone = profile.phone,
            logoUri = profile.logoUri,
            qrisUri = profile.qrisImageUri
        )
    }

    fun updateStoreQrisImage(uri: String?) {
        val updated = _storeProfile.value.copy(qrisImageUri = uri)
        _storeProfile.value = updated
        val authPrefs = AuthPreferences(getApplication())
        authPrefs.storeQrisImageUri = uri
    }

    // CART STATE
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _redeemedPoints = MutableStateFlow(0)
    val redeemedPoints: StateFlow<Int> = _redeemedPoints.asStateFlow()

    private val _transactionDiscountPercent = MutableStateFlow(0.0)
    val transactionDiscountPercent: StateFlow<Double> = _transactionDiscountPercent.asStateFlow()

    private val _taxEnabled = MutableStateFlow(true)
    val taxEnabled: StateFlow<Boolean> = _taxEnabled.asStateFlow()

    private val _serviceEnabled = MutableStateFlow(false)
    val serviceEnabled: StateFlow<Boolean> = _serviceEnabled.asStateFlow()

    private val _transactionNote = MutableStateFlow("")
    val transactionNote: StateFlow<String> = _transactionNote.asStateFlow()

    // Calculated totals
    val cartSubtotal: Double
        get() = _cartItems.value.sumOf { it.totalPrice }

    val cartDiscountAmount: Double
        get() = (cartSubtotal * (_transactionDiscountPercent.value / 100.0)) + (_redeemedPoints.value * 100.0)

    val cartTaxAmount: Double
        get() = if (_taxEnabled.value) ((cartSubtotal - cartDiscountAmount).coerceAtLeast(0.0) * (_storeProfile.value.taxPercent / 100.0)) else 0.0

    val cartServiceAmount: Double
        get() = if (_serviceEnabled.value) ((cartSubtotal - cartDiscountAmount).coerceAtLeast(0.0) * (_storeProfile.value.servicePercent / 100.0)) else 0.0

    val cartGrandTotal: Double
        get() = ((cartSubtotal - cartDiscountAmount).coerceAtLeast(0.0) + cartTaxAmount + cartServiceAmount)

    fun addToCart(product: Product) {
        val current = _cartItems.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.product.id == product.id }
        if (existingIndex >= 0) {
            val item = current[existingIndex]
            current[existingIndex] = item.copy(quantity = item.quantity + 1)
        } else {
            current.add(CartItem(product = product, quantity = 1))
        }
        _cartItems.value = current
    }

    fun updateCartItemQuantity(item: CartItem, delta: Int) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == item.product.id }
        if (index >= 0) {
            val newQty = current[index].quantity + delta
            if (newQty <= 0) {
                current.removeAt(index)
            } else {
                current[index] = current[index].copy(quantity = newQty)
            }
            _cartItems.value = current
        }
    }

    fun removeCartItem(item: CartItem) {
        val current = _cartItems.value.toMutableList()
        current.removeAll { it.product.id == item.product.id }
        _cartItems.value = current
    }

    fun updateItemDiscount(item: CartItem, percent: Double, fixed: Double) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == item.product.id }
        if (index >= 0) {
            current[index] = current[index].copy(discountPercent = percent, discountFixed = fixed)
            _cartItems.value = current
        }
    }

    fun updateItemNote(item: CartItem, note: String) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == item.product.id }
        if (index >= 0) {
            current[index] = current[index].copy(itemNote = note)
            _cartItems.value = current
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _selectedCustomer.value = null
        _redeemedPoints.value = 0
        _transactionDiscountPercent.value = 0.0
        _transactionNote.value = ""
    }

    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
        _redeemedPoints.value = 0
    }

    fun setRedeemPoints(points: Int) {
        val maxPoints = _selectedCustomer.value?.points ?: 0
        _redeemedPoints.value = points.coerceIn(0, maxPoints)
    }

    fun setTransactionDiscountPercent(pct: Double) {
        _transactionDiscountPercent.value = pct.coerceIn(0.0, 100.0)
    }

    fun toggleTax(enabled: Boolean) { _taxEnabled.value = enabled }
    fun toggleService(enabled: Boolean) { _serviceEnabled.value = enabled }
    fun setTransactionNote(note: String) { _transactionNote.value = note }

    // DRAFT / HOLD ORDER
    fun holdOrder(draftTag: String) {
        if (_cartItems.value.isEmpty()) return
        val orderId = "DFT-" + UUID.randomUUID().toString().take(8).uppercase()
        val draft = OrderEntity(
            orderId = orderId,
            cashierName = _currentUser.value.name,
            customerName = _selectedCustomer.value?.name ?: "Draft Customer",
            subtotal = cartSubtotal,
            discountTotal = cartDiscountAmount,
            taxPercent = if (_taxEnabled.value) _storeProfile.value.taxPercent else 0.0,
            taxAmount = cartTaxAmount,
            servicePercent = if (_serviceEnabled.value) _storeProfile.value.servicePercent else 0.0,
            serviceAmount = cartServiceAmount,
            grandTotal = cartGrandTotal,
            status = OrderStatus.HELD_DRAFT.name,
            draftTag = if (draftTag.isBlank()) "Meja ${draftOrders.value.size + 1}" else draftTag,
            itemsJson = PosRepository.serializeItems(_cartItems.value)
        )
        viewModelScope.launch {
            repository.saveDraftOrder(draft)
            clearCart()
        }
    }

    fun restoreDraft(draft: OrderEntity) {
        val restoredItems = PosRepository.deserializeItems(draft.itemsJson, products.value)
        _cartItems.value = restoredItems
        _transactionDiscountPercent.value = 0.0
        _taxEnabled.value = draft.taxAmount > 0
        _serviceEnabled.value = draft.serviceAmount > 0
        viewModelScope.launch {
            repository.deleteDraftOrder(draft)
        }
    }

    // CHECKOUT
    private val _lastCompletedOrder = MutableStateFlow<OrderEntity?>(null)
    val lastCompletedOrder: StateFlow<OrderEntity?> = _lastCompletedOrder.asStateFlow()

    private val _lastCompletedItems = MutableStateFlow<List<CartItem>>(emptyList())
    val lastCompletedItems: StateFlow<List<CartItem>> = _lastCompletedItems.asStateFlow()

    private val _showReceiptDialog = MutableStateFlow(false)
    val showReceiptDialog: StateFlow<Boolean> = _showReceiptDialog.asStateFlow()
    fun dismissReceiptDialog() { _showReceiptDialog.value = false }

    fun processCheckout(
        method: PaymentMethod,
        cashPaid: Double = 0.0,
        splitMethod2: String = "",
        splitAmt1: Double = 0.0,
        splitAmt2: Double = 0.0
    ) {
        val grandTotal = cartGrandTotal
        val change = if (method == PaymentMethod.CASH) (cashPaid - grandTotal).coerceAtLeast(0.0) else 0.0
        val orderId = "ORD-" + System.currentTimeMillis().toString().takeLast(8)
        val activeShift = shifts.value.find { it.status == "OPEN" }

        val order = OrderEntity(
            orderId = orderId,
            timestamp = System.currentTimeMillis(),
            cashierName = _currentUser.value.name,
            cashierRole = _currentUser.value.role.label,
            shiftName = activeShift?.let { "${it.shiftScheduleName} (${it.shiftScheduleTime})" } ?: "",
            shiftId = activeShift?.id ?: 0L,
            customerId = _selectedCustomer.value?.id,
            customerName = _selectedCustomer.value?.name ?: "Pelanggan Walk-In",
            customerPhone = _selectedCustomer.value?.phone ?: "",
            subtotal = cartSubtotal,
            discountTotal = cartDiscountAmount,
            taxPercent = if (_taxEnabled.value) _storeProfile.value.taxPercent else 0.0,
            taxAmount = cartTaxAmount,
            servicePercent = if (_serviceEnabled.value) _storeProfile.value.servicePercent else 0.0,
            serviceAmount = cartServiceAmount,
            grandTotal = grandTotal,
            paymentMethod = method.name,
            cashReceived = cashPaid,
            changeGiven = change,
            splitMethod2 = splitMethod2,
            splitAmount1 = splitAmt1,
            splitAmount2 = splitAmt2,
            orderNote = _transactionNote.value,
            status = OrderStatus.COMPLETED.name,
            itemsJson = PosRepository.serializeItems(_cartItems.value)
        )

        val itemsToKeep = _cartItems.value.toList()
        viewModelScope.launch {
            repository.checkoutOrder(
                order = order,
                cartItems = itemsToKeep,
                customer = _selectedCustomer.value,
                redeemedPoints = _redeemedPoints.value
            )
            _lastCompletedOrder.value = order
            _lastCompletedItems.value = itemsToKeep
            _showReceiptDialog.value = true
            clearCart()
        }
    }

    // SCANNER HOOK
    data class ScanFeedback(
        val barcode: String,
        val product: Product?,
        val timestamp: Long = System.currentTimeMillis()
    )

    data class CapturedProductCode(
        val code: String,
        val product: Product?,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val _isScannerActive = MutableStateFlow(false)
    val isScannerActive: StateFlow<Boolean> = _isScannerActive.asStateFlow()
    fun toggleScanner(active: Boolean) { _isScannerActive.value = active }

    private val _lastScannedBarcode = MutableStateFlow("")
    val lastScannedBarcode: StateFlow<String> = _lastScannedBarcode.asStateFlow()

    private val _lastScannedProduct = MutableStateFlow<Product?>(null)
    val lastScannedProduct: StateFlow<Product?> = _lastScannedProduct.asStateFlow()

    private val _capturedProductCodes = MutableStateFlow<List<CapturedProductCode>>(emptyList())
    val capturedProductCodes: StateFlow<List<CapturedProductCode>> = _capturedProductCodes.asStateFlow()

    fun clearCapturedCodes() {
        _capturedProductCodes.value = emptyList()
        _lastScannedBarcode.value = ""
        _lastScannedProduct.value = null
    }

    fun removeCapturedCode(code: String) {
        _capturedProductCodes.value = _capturedProductCodes.value.filterNot { it.code == code }
    }

    private val _scanFeedback = MutableStateFlow<ScanFeedback?>(null)
    val scanFeedback: StateFlow<ScanFeedback?> = _scanFeedback.asStateFlow()

    fun clearScanFeedback() { _scanFeedback.value = null }

    fun onScanBarcode(barcode: String) {
        val trimmed = barcode.trim()
        if (trimmed.isBlank()) return
        _lastScannedBarcode.value = trimmed
        SoundHelper.playBeep()
        SoundHelper.vibrate(getApplication())

        viewModelScope.launch {
            val product = repository.findProductByBarcodeOrSku(trimmed)
            _lastScannedProduct.value = product
            _scanFeedback.value = ScanFeedback(trimmed, product)

            val entry = CapturedProductCode(trimmed, product)
            val filtered = _capturedProductCodes.value.filterNot { it.code == trimmed }
            _capturedProductCodes.value = (listOf(entry) + filtered).take(12)

            if (product != null) {
                addToCart(product)
            }
        }
    }

    // BARCODE ENGINE STATE
    private val _selectedProductForBarcode = MutableStateFlow<Product?>(null)
    val selectedProductForBarcode: StateFlow<Product?> = _selectedProductForBarcode.asStateFlow()

    private val _barcodeFormat = MutableStateFlow(BarcodeFormat.CODE_128)
    val barcodeFormat: StateFlow<BarcodeFormat> = _barcodeFormat.asStateFlow()

    private val _customBarcodeCode = MutableStateFlow("")
    val customBarcodeCode: StateFlow<String> = _customBarcodeCode.asStateFlow()

    private val _printLayoutPreset = MutableStateFlow("58mm") // 58mm, 80mm, A4_GRID
    val printLayoutPreset: StateFlow<String> = _printLayoutPreset.asStateFlow()

    private val _printSimulationMessage = MutableStateFlow<String?>(null)
    val printSimulationMessage: StateFlow<String?> = _printSimulationMessage.asStateFlow()
    fun clearPrintMessage() { _printSimulationMessage.value = null }

    fun selectProductForBarcode(prod: Product?) {
        _selectedProductForBarcode.value = prod
        if (prod != null) {
            _customBarcodeCode.value = prod.barcode.ifBlank { prod.sku }
        }
    }

    fun setBarcodeFormat(format: BarcodeFormat) { _barcodeFormat.value = format }
    fun setCustomBarcodeCode(code: String) { _customBarcodeCode.value = code }
    fun setPrintLayoutPreset(preset: String) { _printLayoutPreset.value = preset }

    fun printBarcodeLabel() {
        _printSimulationMessage.value = "Berhasil mengirim stream ESC/POS ke Printer Bluetooth (${_printLayoutPreset.value})!"
        SoundHelper.playBeep()
    }

    // SHIFT & CASH REGISTER
    fun openShift(
        startingFloat: Double,
        cashierName: String = _currentUser.value.name,
        shiftScheduleName: String = "Shift 1 (Pagi)",
        shiftScheduleTime: String = "07:00 - 15:00"
    ) {
        val selectedUser = staffUsers.value.find { it.name == cashierName } ?: _currentUser.value
        viewModelScope.launch {
            repository.openShift(
                cashierName = cashierName,
                startingCash = startingFloat,
                shiftScheduleName = shiftScheduleName,
                shiftScheduleTime = shiftScheduleTime,
                cashierId = selectedUser.id,
                cashierRole = selectedUser.role.label
            )
        }
    }

    fun closeShift(shift: Shift, actualCash: Double, notes: String) {
        viewModelScope.launch {
            repository.closeShift(shift, actualCash, notes)
        }
    }

    fun logCashInOut(amount: Double, isCashIn: Boolean, reason: String) {
        val activeShift = shifts.value.find { it.status == "OPEN" } ?: return
        viewModelScope.launch {
            repository.recordCashInOut(activeShift.id, amount, isCashIn, reason)
        }
    }

    // PRODUCT CRUD
    fun saveProduct(product: Product) {
        viewModelScope.launch {
            if (product.id == 0L) {
                repository.insertProduct(product)
            } else {
                repository.updateProduct(product)
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    // INVENTORY ADJUSTMENT & PO
    fun performStockAdjustment(productId: Long, productName: String, type: String, qtyChange: Int, reason: String) {
        viewModelScope.launch {
            repository.adjustStock(
                productId = productId,
                productName = productName,
                type = type,
                qtyChange = qtyChange,
                reason = reason,
                staffName = _currentUser.value.name
            )
        }
    }

    fun createPurchaseOrder(poNumber: String, supplier: String, cost: Double, summary: String, notes: String) {
        viewModelScope.launch {
            repository.insertPurchaseOrder(
                PurchaseOrder(
                    poNumber = poNumber,
                    supplierName = supplier,
                    totalCost = cost,
                    itemsSummary = summary,
                    notes = notes
                )
            )
        }
    }

    fun markPOReceived(po: PurchaseOrder) {
        viewModelScope.launch {
            repository.markPurchaseOrderReceived(po)
        }
    }

    // CRM
    fun saveCustomer(customer: Customer) {
        viewModelScope.launch {
            if (customer.id == 0L) {
                repository.insertCustomer(customer)
            } else {
                repository.updateCustomer(customer)
            }
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    fun payDebt(debt: CustomerDebt, customer: Customer) {
        viewModelScope.launch {
            repository.payCustomerDebt(debt, customer)
        }
    }

    // STAFF LOGIN / ROLE SWITCH
    fun switchUser(staffUser: StaffUser) {
        _currentUser.value = staffUser
    }

    fun saveStaffUser(user: StaffUser) {
        viewModelScope.launch {
            if (user.id.isBlank()) {
                val newId = "U-${(System.currentTimeMillis() % 1000000).toString().padStart(6, '0')}"
                repository.insertStaffUser(user.copy(id = newId))
            } else {
                repository.insertStaffUser(user)
            }
        }
    }

    fun deleteStaffUser(user: StaffUser) {
        viewModelScope.launch {
            repository.deleteStaffUser(user)
            if (_currentUser.value.id == user.id) {
                val remaining = staffUsers.value.filter { it.id != user.id }
                if (remaining.isNotEmpty()) {
                    _currentUser.value = remaining.first()
                }
            }
        }
    }

    fun saveShiftSchedule(schedule: ShiftSchedule) {
        viewModelScope.launch {
            if (schedule.id == 0L) {
                repository.insertShiftSchedule(schedule)
            } else {
                repository.updateShiftSchedule(schedule)
            }
        }
    }

    fun deleteShiftSchedule(schedule: ShiftSchedule) {
        viewModelScope.launch {
            repository.deleteShiftSchedule(schedule)
        }
    }
}
