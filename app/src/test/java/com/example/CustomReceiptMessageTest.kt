package com.example

import com.example.data.model.BusinessType
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

    private val sampleOnlineStore = StoreProfile(
        storeName = "VORAVIO FASHION ONLINE",
        businessType = BusinessType.ONLINE,
        onlineStoreLink = "shopee.co.id/voravio",
        instagram = "@voravio.online",
        phone = "081299887766",
        receiptFooter = "Terima kasih telah berbelanja online!"
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

    @Test
    fun testOnlineReceiptHasNoCashierNameAndNoWalkInAndHasRefundWarning() {
        val customNote = "Paket dipacking aman lapis bubble wrap tebal!"
        val onlineOrder = sampleOrder.copy(
            cashierName = "Staf Admin Online",
            customerName = "Pelanggan Walk-In",
            orderNote = customNote
        )

        val receiptText = EscPosPrinterHelper.formatReceiptText(
            order = onlineOrder,
            items = sampleItems,
            store = sampleOnlineStore,
            customReceiptNote = customNote
        )

        // 1. Tidak ada nama kasir
        assertFalse("Struk online tidak boleh memuat kata 'Kasir'", receiptText.contains("Kasir     :"))
        assertFalse("Struk online tidak boleh memuat 'Admin Olshop'", receiptText.contains("Admin Olshop"))
        assertFalse("Struk online tidak boleh memuat nama kasir", receiptText.contains("Staf Admin Online"))

        // 2. Tidak ada customer walk in
        assertFalse("Struk online tidak boleh memuat 'Walk-In'", receiptText.contains("Walk-In"))
        assertTrue("Struk online harus memuat 'Pelanggan Online'", receiptText.contains("Pelanggan Online"))

        // 3. Tetap ada custom pesan yang bisa diketik sendiri
        assertTrue("Struk online harus memuat custom pesan", receiptText.contains("Paket dipacking aman"))

        // 4. Ada teks tebal bold besar semua peringatan unboxing
        assertTrue(
            "Struk online harus memuat teks peringatan refund",
            receiptText.contains("!!PERHATIAN JANGAN SAMPAI HILANG")
        )
        assertTrue(
            "Struk online harus memuat instruksi video unboxing untuk refund",
            receiptText.contains("UNBOXING KETIKA INGIN REFUND!!")
        )
    }

    @Test
    fun testOfflineReceiptRetainsCashierAndCustomer() {
        val receiptText = EscPosPrinterHelper.formatReceiptText(
            order = sampleOrder,
            items = sampleItems,
            store = sampleStore
        )

        assertTrue("Struk offline harus memiliki nama kasir", receiptText.contains("Kasir     :"))
        assertTrue("Struk offline harus mencantumkan Budi Kasir", receiptText.contains("Budi Kasir"))
        assertFalse(
            "Struk offline tidak boleh memuat peringatan unboxing online",
            receiptText.contains("!!PERHATIAN JANGAN SAMPAI HILANG STRUK INI DAN SERTAKAN VIDEO UNBOXING KETIKA INGIN REFUND!!")
        )
    }
}
