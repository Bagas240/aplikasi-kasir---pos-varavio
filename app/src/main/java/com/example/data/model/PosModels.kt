package com.example.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class UserRole(val label: String) {
    OWNER("Owner / Pemilik"),
    MANAGER("Manajer"),
    CASHIER("Kasir")
}

@Immutable
@Entity(tableName = "staff_users")
data class StaffUser(
    @PrimaryKey
    val id: String,
    val name: String,
    val role: UserRole,
    val pin: String,
    val phone: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Immutable
@Entity(tableName = "shift_schedules")
data class ShiftSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val startTime: String = "07:00",
    val endTime: String = "15:00",
    val isActive: Boolean = true,
    val notes: String = ""
)

@Immutable
data class ProductVariant(
    val id: String,
    val name: String,
    val sku: String,
    val priceDiff: Double = 0.0,
    val stock: Int = 10
)

@Immutable
@Entity(
    tableName = "products",
    indices = [
        Index(value = ["barcode"]),
        Index(value = ["sku"]),
        Index(value = ["category"]),
        Index(value = ["isActive"])
    ]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sku: String,
    val barcode: String,
    val category: String,
    val unit: String = "Pcs",
    val buyPrice: Double,
    val sellPrice: Double,
    val stock: Int,
    val minStockAlert: Int = 5,
    val description: String = "",
    val hasWholesale: Boolean = false,
    val wholesaleMinQty: Int = 5,
    val wholesalePrice: Double = 0.0,
    val variantsJson: String = "[]",
    val iconColor: Long = 0xFF1E40AF,
    val imageUri: String = "",
    val isActive: Boolean = true
) {
    val profit: Double get() = sellPrice - buyPrice
    val marginPercent: Double
        get() = if (sellPrice > 0) ((sellPrice - buyPrice) / sellPrice) * 100 else 0.0
    val isLowStock: Boolean get() = stock <= minStockAlert
    val isOutOfStock: Boolean get() = stock <= 0
}

@Immutable
data class CartItem(
    val product: Product,
    val selectedVariant: ProductVariant? = null,
    val quantity: Int = 1,
    val discountPercent: Double = 0.0,
    val discountFixed: Double = 0.0,
    val itemNote: String = ""
) {
    val unitPrice: Double
        get() {
            val base = product.sellPrice + (selectedVariant?.priceDiff ?: 0.0)
            if (product.hasWholesale && quantity >= product.wholesaleMinQty && product.wholesalePrice > 0) {
                return product.wholesalePrice + (selectedVariant?.priceDiff ?: 0.0)
            }
            return base
        }

    val discountAmount: Double
        get() {
            val baseTotal = unitPrice * quantity
            val pctDiscount = baseTotal * (discountPercent / 100.0)
            return (pctDiscount + (discountFixed * quantity)).coerceAtMost(baseTotal)
        }

    val totalPrice: Double
        get() = (unitPrice * quantity) - discountAmount

    val displayName: String
        get() = if (selectedVariant != null) "${product.name} (${selectedVariant.name})" else product.name

    val displaySku: String
        get() = if (selectedVariant != null) selectedVariant.sku else product.sku
}

enum class PaymentMethod(val label: String) {
    CASH("Cash / Tunai"),
    QRIS("QRIS"),
    DEBIT_CARD("Debit Card"),
    CREDIT_CARD("Credit Card"),
    E_WALLET("E-Wallet"),
    BANK_TRANSFER("Bank Transfer"),
    SPLIT("Split Payment"),
    DEBT("Customer Tab (Piutang)")
}

enum class OrderStatus {
    COMPLETED,
    HELD_DRAFT,
    VOID,
    UNPAID
}

@Immutable
@Entity(
    tableName = "orders",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["shiftId"]),
        Index(value = ["status"])
    ]
)
data class OrderEntity(
    @PrimaryKey
    val orderId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val cashierName: String = "Kasir 1",
    val customerId: Long? = null,
    val customerName: String = "Walk-in Customer",
    val customerPhone: String = "",
    val subtotal: Double,
    val discountTotal: Double = 0.0,
    val taxPercent: Double = 0.0,
    val taxAmount: Double = 0.0,
    val servicePercent: Double = 0.0,
    val serviceAmount: Double = 0.0,
    val grandTotal: Double,
    val paymentMethod: String = PaymentMethod.CASH.name,
    val cashReceived: Double = 0.0,
    val changeGiven: Double = 0.0,
    val splitMethod2: String = "",
    val splitAmount1: Double = 0.0,
    val splitAmount2: Double = 0.0,
    val orderNote: String = "",
    val status: String = OrderStatus.COMPLETED.name,
    val draftTag: String = "", // e.g. "Table 4"
    val itemsJson: String = "[]",
    val shiftId: Long = 0L,
    val shiftName: String = "",
    val cashierRole: String = "Kasir"
)

@Immutable
@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val address: String = "",
    val points: Int = 0,
    val debtBalance: Double = 0.0,
    val totalSpend: Double = 0.0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Immutable
@Entity(tableName = "customer_debts")
data class CustomerDebt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val orderId: String,
    val amount: Double,
    val dueDate: Long,
    val status: String = "UNPAID", // UNPAID, PAID
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Immutable
@Entity(tableName = "stock_adjustments")
data class StockAdjustment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val type: String, // ADDITION, DAMAGE_WASTE, CORRECTION, TRANSFER
    val qtyChange: Int,
    val previousStock: Int,
    val newStock: Int,
    val reason: String,
    val staffName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Immutable
@Entity(tableName = "purchase_orders")
data class PurchaseOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val poNumber: String,
    val supplierName: String,
    val supplierContact: String = "",
    val status: String = "PENDING", // PENDING, RECEIVED, CANCELLED
    val totalCost: Double,
    val itemsSummary: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Immutable
@Entity(
    tableName = "shifts",
    indices = [
        Index(value = ["status"]),
        Index(value = ["startTime"])
    ]
)
data class Shift(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shiftScheduleName: String = "Shift 1 (Pagi)",
    val shiftScheduleTime: String = "07:00 - 15:00",
    val cashierName: String,
    val cashierId: String = "",
    val cashierRole: String = "Kasir",
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val startingCash: Double,
    val cashSales: Double = 0.0,
    val digitalSales: Double = 0.0,
    val cashIn: Double = 0.0,
    val cashOut: Double = 0.0,
    val expectedCash: Double = startingCash,
    val actualCash: Double? = null,
    val difference: Double? = null,
    val status: String = "OPEN", // OPEN, CLOSED
    val notes: String = ""
)

@Immutable
@Entity(
    tableName = "transaction_logs",
    indices = [
        Index(value = ["orderId"]),
        Index(value = ["timestamp"]),
        Index(value = ["status"]),
        Index(value = ["cashierName"])
    ]
)
data class TransactionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val cashierName: String = "Kasir 1",
    val cashierRole: String = "Kasir",
    val shiftName: String = "",
    val customerName: String = "Walk-in Customer",
    val paymentMethod: String = "CASH",
    val subtotal: Double = 0.0,
    val taxAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val cashReceived: Double = 0.0,
    val changeGiven: Double = 0.0,
    val totalItems: Int = 0,
    val itemsSummary: String = "",
    val status: String = "COMPLETED", // COMPLETED, VOID, REFUNDED
    val notes: String = ""
)

@Immutable
data class StoreProfile(
    val storeName: String = "VORAVIO MART",
    val address: String = "Jl. Thamrin No. 88, Jakarta Pusat",
    val phone: String = "+62 812-3456-7890",
    val instagram: String = "@voraviopos.id",
    val logoUri: String? = null,
    val qrisImageUri: String? = null,
    val receiptHeader: String = "STRUK PEMBELIAN RESMI",
    val receiptFooter: String = "Terima kasih atas kunjungan Anda!\nBarang yang sudah dibeli tidak dapat ditukar.",
    val taxPercent: Double = 11.0,
    val taxEnabled: Boolean = true,
    val servicePercent: Double = 0.0,
    val serviceEnabled: Boolean = false,
    val printerPaperWidth: String = "58mm", // 58mm or 80mm
    val loyaltyPointRate: Int = 1000 // 1 pt per Rp 1000
)
