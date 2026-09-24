package com.example

import com.example.data.model.OrderEntity
import com.example.data.model.Product
import com.example.data.model.StoreProfile
import com.example.util.CsvReportGenerator
import com.example.util.ReportPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CsvReportGeneratorTest {

    private val sampleStoreProfile = StoreProfile(
        storeName = "VORAVIO MART",
        address = "Jl. Gatot Subroto No. 88, Jakarta",
        phone = "081234567890",
        receiptFooter = "Terima kasih atas kunjungan Anda"
    )

    private val sampleProducts = listOf(
        Product(
            id = 101L,
            name = "Kopi Susu Gula Aren",
            sku = "COF-001",
            barcode = "899123456001",
            category = "Beverage",
            buyPrice = 8000.0,
            sellPrice = 18000.0,
            stock = 50
        ),
        Product(
            id = 102L,
            name = "Croissant Butter",
            sku = "BAK-002",
            barcode = "899123456002",
            category = "Bakery",
            buyPrice = 12000.0,
            sellPrice = 25000.0,
            stock = 30
        )
    )

    private fun createSampleOrder(
        orderId: String,
        timestamp: Long,
        subtotal: Double,
        discount: Double = 0.0,
        tax: Double = 0.0,
        grandTotal: Double = subtotal - discount + tax,
        paymentMethod: String = "CASH",
        itemsJson: String = """[{"productId":101,"name":"Kopi Susu Gula Aren","quantity":2,"unitPrice":18000.0,"discountAmount":0.0,"totalPrice":36000.0}]"""
    ): OrderEntity {
        return OrderEntity(
            orderId = orderId,
            timestamp = timestamp,
            cashierName = "Budi Santoso",
            cashierRole = "Kasir",
            customerName = "Ahmad Dani",
            customerPhone = "08111222333",
            subtotal = subtotal,
            discountTotal = discount,
            taxAmount = tax,
            grandTotal = grandTotal,
            paymentMethod = paymentMethod,
            cashReceived = grandTotal + 10000.0,
            changeGiven = 10000.0,
            itemsJson = itemsJson
        )
    }

    @Test
    fun testEscapeCsvProperlyHandlesSpecialCharacters() {
        assertEquals("SimpleText", CsvReportGenerator.escapeCsv("SimpleText"))
        assertEquals("\"Text, with comma\"", CsvReportGenerator.escapeCsv("Text, with comma"))
        assertEquals("\"Text \"\"with\"\" quotes\"", CsvReportGenerator.escapeCsv("Text \"with\" quotes"))
        assertEquals("Text; without comma", CsvReportGenerator.escapeCsv("Text; without comma"))
        assertEquals("\"Line1\nLine2\"", CsvReportGenerator.escapeCsv("Line1\nLine2"))
    }

    @Test
    fun testFilterOrdersByTodayAndAll() {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, -5)
        val fiveDaysAgo = calendar.timeInMillis

        val orderToday = createSampleOrder("ORD-001", now, 36000.0)
        val orderPast = createSampleOrder("ORD-002", fiveDaysAgo, 50000.0)
        val orders = listOf(orderToday, orderPast)

        val todayFiltered = CsvReportGenerator.filterOrders(orders, ReportPeriod.TODAY)
        assertEquals(1, todayFiltered.size)
        assertEquals("ORD-001", todayFiltered[0].orderId)

        val allFiltered = CsvReportGenerator.filterOrders(orders, ReportPeriod.ALL_TIME)
        assertEquals(2, allFiltered.size)
    }

    @Test
    fun testComputeSummaryCalculatesFinancialMetricsAccurately() {
        val now = System.currentTimeMillis()
        val order1 = createSampleOrder(
            orderId = "ORD-001",
            timestamp = now,
            subtotal = 36000.0,
            discount = 2000.0,
            tax = 3400.0,
            grandTotal = 37400.0,
            paymentMethod = "QRIS",
            itemsJson = """[{"productId":101,"name":"Kopi Susu Gula Aren","quantity":2,"unitPrice":18000.0,"discountAmount":2000.0,"totalPrice":34000.0}]"""
        )

        val order2 = createSampleOrder(
            orderId = "ORD-002",
            timestamp = now,
            subtotal = 50000.0,
            discount = 0.0,
            tax = 0.0,
            grandTotal = 50000.0,
            paymentMethod = "CASH",
            itemsJson = """[{"productId":102,"name":"Croissant Butter","quantity":2,"unitPrice":25000.0,"discountAmount":0.0,"totalPrice":50000.0}]"""
        )

        val summary = CsvReportGenerator.computeSummary(listOf(order1, order2), sampleProducts, "Hari Ini")

        assertEquals(2, summary.totalOrders)
        assertEquals(86000.0, summary.grossSales, 0.01)
        assertEquals(2000.0, summary.totalDiscount, 0.01)
        assertEquals(3400.0, summary.totalTax, 0.01)
        assertEquals(87400.0, summary.netSales, 0.01)

        // COGS: 2x 8000 (Kopi) + 2x 12000 (Croissant) = 16000 + 24000 = 40000
        assertEquals(40000.0, summary.totalCogs, 0.01)
        // Gross Profit: 87400 - 40000 = 47400
        assertEquals(47400.0, summary.grossProfit, 0.01)
        // Profit Margin: (47400 / 87400) * 100
        assertTrue(summary.profitMarginPercent > 50.0)
        // Average Order Value (AOV): 87400 / 2 = 43700
        assertEquals(43700.0, summary.averageOrderValue, 0.01)
    }

    @Test
    fun testGenerateComprehensiveCsvContainsExpectedSections() {
        val now = System.currentTimeMillis()
        val order = createSampleOrder("ORD-001", now, 36000.0, 0.0, 0.0, 36000.0, "CASH")
        val csv = CsvReportGenerator.generateComprehensiveCsv(
            storeProfile = sampleStoreProfile,
            orders = listOf(order),
            products = sampleProducts,
            periodLabel = "Hari Ini"
        )

        assertNotNull(csv)
        assertTrue(csv.contains("LAPORAN PENJUALAN & AKUNTANSI HARIAN"))
        assertTrue(csv.contains("VORAVIO MART"))
        assertTrue(csv.contains("RINGKASAN EKSEKUTIF KEUANGAN"))
        assertTrue(csv.contains("RINCIAN METODE PEMBAYARAN"))
        assertTrue(csv.contains("DETAIL TRANSAKSI PENJUALAN"))
        assertTrue(csv.contains("ORD-001"))
        assertTrue(csv.contains("Budi Santoso"))
    }

    @Test
    fun testGenerateTransactionsOnlyCsvOutputsValidHeaderAndData() {
        val now = System.currentTimeMillis()
        val order = createSampleOrder("ORD-001", now, 36000.0)
        val csv = CsvReportGenerator.generateTransactionsOnlyCsv(
            orders = listOf(order),
            products = sampleProducts
        )

        assertTrue(csv.contains("No_Nota,Tanggal,Waktu,Timestamp_Millis,Kasir"))
        assertTrue(csv.contains("ORD-001"))
    }

    @Test
    fun testGenerateProductPerformanceCsvContainsMetrics() {
        val now = System.currentTimeMillis()
        val order = createSampleOrder("ORD-001", now, 36000.0)
        val csv = CsvReportGenerator.generateProductPerformanceCsv(
            orders = listOf(order),
            products = sampleProducts,
            periodLabel = "Hari Ini"
        )

        assertTrue(csv.contains("Kopi Susu Gula Aren"))
        assertTrue(csv.contains("COF-001"))
        assertTrue(csv.contains("Beverage"))
    }
}
