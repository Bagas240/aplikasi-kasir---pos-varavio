package com.example

import com.example.data.model.CartItem
import com.example.data.model.OrderEntity
import com.example.data.model.Product
import com.example.data.model.StoreProfile
import com.example.util.EscPosPrinterHelper
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CustomReceiptMessageTest {

    private val sampleStore = StoreProfile(
        storeName = "VORAVIO POS",
        address = "Jl. Sudirman No. 10",
        phone = "08123456789",
        receiptFooter = "Terima kasih atas kunjungan Anda"
    )

    private val sampleProduct = Product(
        id = 1L,
        name = "Kopi Susu",
        sku = "KOP-01",
        barcode = "8990001",
        category = "Minuman",
        buyPrice = 10000.0,
        sellPrice = 18000.0,
        stock = 20
    )

    private val sampleOrder = OrderEntity(
        orderId = "ORD-TEST-01",
        cashierName = "Budi Kasir",
        customerName = "Kak Sarah",
        subtotal = 18000.0,
        grandTotal = 18000.0,
        orderNote = "Selamat menikmati pesanan Anda!"
    )

    private val sampleItems = listOf(
        CartItem(product = sampleProduct, quantity = 1)
    )

    @Test
    fun testCustomReceiptMessageAppearsOnReceiptText() {
        val customNote = "Terima kasih Kak Sarah, semoga harinya bahagia!"
        val receiptText = EscPosPrinterHelper.formatReceiptText(
            order = sampleOrder,
            items = sampleItems,
            store = sampleStore,
            customReceiptNote = customNote
        )

        assertTrue(receiptText.contains("PESAN UNTUK PELANGGAN"))
        assertTrue(receiptText.contains("Terima kasih Kak Sarah,"))
        assertTrue(receiptText.contains("Kopi Susu"))
        assertTrue(receiptText.contains("VORAVIO POS"))
    }

    @Test
    fun testEmptyCustomMessageOmitsMessageSection() {
        val receiptText = EscPosPrinterHelper.formatReceiptText(
            order = sampleOrder.copy(orderNote = ""),
            items = sampleItems,
            store = sampleStore,
            customReceiptNote = ""
        )

        assertFalse(receiptText.contains("PESAN UNTUK PELANGGAN"))
        assertTrue(receiptText.contains("Terima kasih atas kunjungan Anda"))
    }

    @Test
    fun testEscPosBinaryIncludesCustomMessage() {
        val customNote = "Semoga harinya menyenangkan!"
        val bytes = EscPosPrinterHelper.generateEscPosBytes(
            order = sampleOrder,
            items = sampleItems,
            store = sampleStore,
            customReceiptNote = customNote
        )

        assertTrue(bytes.isNotEmpty())
        val str = String(bytes, Charsets.ISO_8859_1)
        assertTrue(str.contains("Semoga harinya menyenangkan!"))
    }
}
