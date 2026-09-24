package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.OrderEntity
import com.example.data.model.Product
import com.example.data.model.StoreProfile
import com.example.data.repository.PosRepository
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ReportPeriod(val label: String) {
    TODAY("Hari Ini"),
    YESTERDAY("Kemarin"),
    LAST_7_DAYS("7 Hari Terakhir"),
    LAST_30_DAYS("30 Hari Terakhir"),
    THIS_MONTH("Bulan Ini"),
    CUSTOM_DATE("Pilih Tanggal"),
    ALL_TIME("Semua Riwayat")
}

enum class ReportType(val title: String, val description: String) {
    COMPREHENSIVE(
        "Laporan Komprehensif (Akuntansi & Analitik)",
        "KPI keuangan, rekap metode bayar, performa produk & detail transaksi."
    ),
    TRANSACTIONS_ONLY(
        "Tabel Transaksi Lengkap (Orders Log CSV)",
        "Daftar per nota dengan kolom subtotal, diskon, pajak, total & kasir."
    ),
    PRODUCTS_PERFORMANCE(
        "Analisis Performa Produk (Item Sales CSV)",
        "SKU, kategori, qty terjual, omset, modal HPP & margin laba kotor."
    )
}

data class ProductSalesMetric(
    val sku: String,
    val name: String,
    val category: String,
    val quantitySold: Int,
    val unitPrice: Double,
    val unitCost: Double,
    val totalRevenue: Double,
    val totalCost: Double,
    val grossProfit: Double,
    val profitMarginPercent: Double
)

data class SalesReportSummary(
    val periodLabel: String,
    val totalOrders: Int,
    val grossSales: Double,
    val totalDiscount: Double,
    val totalTax: Double,
    val netSales: Double,
    val totalCogs: Double,
    val grossProfit: Double,
    val profitMarginPercent: Double,
    val averageOrderValue: Double,
    val paymentBreakdown: Map<String, Pair<Int, Double>>, // Method -> (Count, TotalAmount)
    val productMetrics: List<ProductSalesMetric>
)

object CsvReportGenerator {

    private val indonesianLocale = Locale("id", "ID")

    /**
     * Filters list of orders by selected period.
     */
    fun filterOrders(
        orders: List<OrderEntity>,
        period: ReportPeriod,
        customDateMillis: Long? = null
    ): List<OrderEntity> {
        val calendar = Calendar.getInstance()

        return when (period) {
            ReportPeriod.TODAY -> {
                val startOfDay = getStartOfDay(calendar.timeInMillis)
                val endOfDay = getEndOfDay(calendar.timeInMillis)
                orders.filter { it.timestamp in startOfDay..endOfDay }
            }
            ReportPeriod.YESTERDAY -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val startOfDay = getStartOfDay(calendar.timeInMillis)
                val endOfDay = getEndOfDay(calendar.timeInMillis)
                orders.filter { it.timestamp in startOfDay..endOfDay }
            }
            ReportPeriod.LAST_7_DAYS -> {
                val end = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val start = getStartOfDay(calendar.timeInMillis)
                orders.filter { it.timestamp in start..end }
            }
            ReportPeriod.LAST_30_DAYS -> {
                val end = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                val start = getStartOfDay(calendar.timeInMillis)
                orders.filter { it.timestamp in start..end }
            }
            ReportPeriod.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = getStartOfDay(calendar.timeInMillis)
                orders.filter { it.timestamp >= start }
            }
            ReportPeriod.CUSTOM_DATE -> {
                val targetMillis = customDateMillis ?: calendar.timeInMillis
                val startOfDay = getStartOfDay(targetMillis)
                val endOfDay = getEndOfDay(targetMillis)
                orders.filter { it.timestamp in startOfDay..endOfDay }
            }
            ReportPeriod.ALL_TIME -> orders
        }
    }

    /**
     * Computes analytical summary from a set of orders and product catalog.
     */
    fun computeSummary(
        orders: List<OrderEntity>,
        products: List<Product>,
        periodLabel: String
    ): SalesReportSummary {
        val totalOrders = orders.size
        var grossSales = 0.0
        var totalDiscount = 0.0
        var totalTax = 0.0
        var netSales = 0.0
        var totalCogs = 0.0

        val productMap = products.associateBy { it.id }
        val productSalesMap = mutableMapOf<Long, MutableProductSales>()

        val paymentCounts = mutableMapOf<String, Pair<Int, Double>>()

        for (order in orders) {
            grossSales += order.subtotal
            totalDiscount += order.discountTotal
            totalTax += order.taxAmount
            netSales += order.grandTotal

            // Payment method breakdown
            val method = order.paymentMethod.ifBlank { "TUNAI" }
            val existing = paymentCounts[method] ?: (0 to 0.0)
            paymentCounts[method] = (existing.first + 1) to (existing.second + order.grandTotal)

            // Item-level deserialization for COGS and product metrics
            val items = PosRepository.deserializeItems(order.itemsJson, products)
            for (item in items) {
                val catalogProduct = productMap[item.product.id] ?: item.product
                val cost = catalogProduct.buyPrice * item.quantity
                totalCogs += cost

                val metrics = productSalesMap.getOrPut(catalogProduct.id) {
                    MutableProductSales(
                        sku = catalogProduct.sku.ifBlank { catalogProduct.barcode },
                        name = catalogProduct.name,
                        category = catalogProduct.category,
                        unitCost = catalogProduct.buyPrice
                    )
                }
                metrics.quantitySold += item.quantity
                metrics.totalRevenue += item.totalPrice
                metrics.totalCost += cost
            }
        }

        val grossProfit = (netSales - totalCogs).coerceAtLeast(0.0)
        val profitMargin = if (netSales > 0.0) (grossProfit / netSales) * 100.0 else 0.0
        val aov = if (totalOrders > 0) netSales / totalOrders else 0.0

        val productMetrics = productSalesMap.values.map { m ->
            val pProfit = (m.totalRevenue - m.totalCost).coerceAtLeast(0.0)
            val pMargin = if (m.totalRevenue > 0.0) (pProfit / m.totalRevenue) * 100.0 else 0.0
            val avgUnitPrice = if (m.quantitySold > 0) m.totalRevenue / m.quantitySold else 0.0
            ProductSalesMetric(
                sku = m.sku,
                name = m.name,
                category = m.category,
                quantitySold = m.quantitySold,
                unitPrice = avgUnitPrice,
                unitCost = m.unitCost,
                totalRevenue = m.totalRevenue,
                totalCost = m.totalCost,
                grossProfit = pProfit,
                profitMarginPercent = pMargin
            )
        }.sortedByDescending { it.totalRevenue }

        return SalesReportSummary(
            periodLabel = periodLabel,
            totalOrders = totalOrders,
            grossSales = grossSales,
            totalDiscount = totalDiscount,
            totalTax = totalTax,
            netSales = netSales,
            totalCogs = totalCogs,
            grossProfit = grossProfit,
            profitMarginPercent = profitMargin,
            averageOrderValue = aov,
            paymentBreakdown = paymentCounts,
            productMetrics = productMetrics
        )
    }

    /**
     * Generates Comprehensive Accounting & Business Analytics CSV report.
     */
    fun generateComprehensiveCsv(
        storeProfile: StoreProfile,
        orders: List<OrderEntity>,
        products: List<Product>,
        periodLabel: String
    ): String {
        val summary = computeSummary(orders, products, periodLabel)
        val sb = StringBuilder()

        // UTF-8 BOM for Microsoft Excel compatibility
        sb.append("\uFEFF")

        // 1. Header Metadata
        val nowFormatted = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", indonesianLocale).format(Date())
        sb.appendLine(csvRow("LAPORAN PENJUALAN & AKUNTANSI HARIAN - ${storeProfile.storeName.uppercase()}"))
        sb.appendLine(csvRow("Alamat:", storeProfile.address))
        sb.appendLine(csvRow("No. Kontak:", storeProfile.phone))
        sb.appendLine(csvRow("Periode Laporan:", periodLabel))
        sb.appendLine(csvRow("Waktu Generate:", nowFormatted))
        sb.appendLine()

        // 2. Executive Financial Summary (KPI)
        sb.appendLine(csvRow("=== RINGKASAN EKSEKUTIF KEUANGAN ==="))
        sb.appendLine(csvRow("Metrik Finansial", "Nilai Angka (IDR)", "Keterangan"))
        sb.appendLine(csvRow("Total Transaksi Selesai", summary.totalOrders.toString(), "Nota Berhasil"))
        sb.appendLine(csvRow("Total Penjualan Kotor (Subtotal)", formatNum(summary.grossSales), CurrencyFormatter.formatRupiah(summary.grossSales)))
        sb.appendLine(csvRow("Total Diskon Diberikan", formatNum(summary.totalDiscount), CurrencyFormatter.formatRupiah(summary.totalDiscount)))
        sb.appendLine(csvRow("Total Pajak / PPN", formatNum(summary.totalTax), CurrencyFormatter.formatRupiah(summary.totalTax)))
        sb.appendLine(csvRow("Total Penjualan Bersih (Net Sales)", formatNum(summary.netSales), CurrencyFormatter.formatRupiah(summary.netSales)))
        sb.appendLine(csvRow("Estimasi Total HPP / Modal Barang (COGS)", formatNum(summary.totalCogs), CurrencyFormatter.formatRupiah(summary.totalCogs)))
        sb.appendLine(csvRow("Estimasi Laba Kotor (Gross Profit)", formatNum(summary.grossProfit), CurrencyFormatter.formatRupiah(summary.grossProfit)))
        sb.appendLine(csvRow("Margin Keuntungan Kotor (%)", String.format(Locale.US, "%.2f%%", summary.profitMarginPercent), "Laba Bersih terhadap Omset"))
        sb.appendLine(csvRow("Rata-rata Belanja per Nota (AOV)", formatNum(summary.averageOrderValue), CurrencyFormatter.formatRupiah(summary.averageOrderValue)))
        sb.appendLine()

        // 3. Payment Methods Breakdown
        sb.appendLine(csvRow("=== RINCIAN METODE PEMBAYARAN ==="))
        sb.appendLine(csvRow("Metode Pembayaran", "Jumlah Transaksi", "Total Nominal (IDR)", "Persentase Omset"))
        summary.paymentBreakdown.forEach { (method, data) ->
            val (count, total) = data
            val pct = if (summary.netSales > 0.0) (total / summary.netSales) * 100.0 else 0.0
            sb.appendLine(csvRow(
                method,
                count.toString(),
                formatNum(total),
                String.format(Locale.US, "%.1f%%", pct)
            ))
        }
        sb.appendLine()

        // 4. Product Sales Performance
        sb.appendLine(csvRow("=== ANALISIS PERFORMA PRODUK TERJUAL ==="))
        sb.appendLine(csvRow(
            "No",
            "SKU",
            "Nama Produk",
            "Kategori",
            "Qty Terjual",
            "Harga Modal Satuan (HPP)",
            "Harga Jual Rata-rata",
            "Total Pendapatan (Omset)",
            "Total Modal (HPP)",
            "Laba Kotor Produk",
            "Margin (%)"
        ))
        summary.productMetrics.forEachIndexed { idx, p ->
            sb.appendLine(csvRow(
                (idx + 1).toString(),
                p.sku,
                p.name,
                p.category,
                p.quantitySold.toString(),
                formatNum(p.unitCost),
                formatNum(p.unitPrice),
                formatNum(p.totalRevenue),
                formatNum(p.totalCost),
                formatNum(p.grossProfit),
                String.format(Locale.US, "%.1f%%", p.profitMarginPercent)
            ))
        }
        sb.appendLine()

        // 5. Detailed Orders Transaction Log
        sb.appendLine(csvRow("=== DETAIL TRANSAKSI PENJUALAN (ORDER LOG) ==="))
        sb.appendLine(csvRow(
            "No Nota",
            "Tanggal",
            "Waktu",
            "Kasir",
            "Role",
            "Shift",
            "Pelanggan",
            "Metode Bayar",
            "Daftar Item",
            "Subtotal (IDR)",
            "Diskon (IDR)",
            "Pajak (IDR)",
            "Total Akhir (IDR)",
            "Jumlah Dibayar (IDR)",
            "Kembalian (IDR)",
            "Status",
            "Catatan"
        ))

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", indonesianLocale)
        val timeFormat = SimpleDateFormat("HH:mm:ss", indonesianLocale)

        for (order in orders) {
            val dateStr = dateFormat.format(Date(order.timestamp))
            val timeStr = timeFormat.format(Date(order.timestamp))
            val items = PosRepository.deserializeItems(order.itemsJson, products)
            val itemsDetail = items.joinToString("; ") { "${it.displayName} (x${it.quantity} @${formatNum(it.unitPrice)})" }

            sb.appendLine(csvRow(
                order.orderId,
                dateStr,
                timeStr,
                order.cashierName.ifBlank { "Kasir" },
                order.cashierRole,
                order.shiftName.ifBlank { "-" },
                order.customerName.ifBlank { "Umum" },
                order.paymentMethod,
                itemsDetail,
                formatNum(order.subtotal),
                formatNum(order.discountTotal),
                formatNum(order.taxAmount),
                formatNum(order.grandTotal),
                formatNum(order.cashReceived),
                formatNum(order.changeGiven),
                order.status,
                order.orderNote
            ))
        }

        return sb.toString()
    }

    /**
     * Generates a clean tabular Transactions-Only CSV.
     */
    fun generateTransactionsOnlyCsv(
        orders: List<OrderEntity>,
        products: List<Product>
    ): String {
        val sb = StringBuilder()
        sb.append("\uFEFF") // UTF-8 BOM

        sb.appendLine(csvRow(
            "No_Nota",
            "Tanggal",
            "Waktu",
            "Timestamp_Millis",
            "Kasir",
            "Role_Kasir",
            "Shift",
            "Pelanggan",
            "Metode_Pembayaran",
            "Rincian_Barang",
            "Subtotal",
            "Diskon",
            "Pajak",
            "Total_Akhir",
            "Dibayar",
            "Kembalian",
            "Status",
            "Catatan"
        ))

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", indonesianLocale)
        val timeFormat = SimpleDateFormat("HH:mm:ss", indonesianLocale)

        for (order in orders) {
            val dateStr = dateFormat.format(Date(order.timestamp))
            val timeStr = timeFormat.format(Date(order.timestamp))
            val items = PosRepository.deserializeItems(order.itemsJson, products)
            val itemsDetail = items.joinToString(" | ") { "${it.displayName} x${it.quantity}" }

            sb.appendLine(csvRow(
                order.orderId,
                dateStr,
                timeStr,
                order.timestamp.toString(),
                order.cashierName.ifBlank { "Kasir" },
                order.cashierRole,
                order.shiftName.ifBlank { "-" },
                order.customerName.ifBlank { "Umum" },
                order.paymentMethod,
                itemsDetail,
                formatNum(order.subtotal),
                formatNum(order.discountTotal),
                formatNum(order.taxAmount),
                formatNum(order.grandTotal),
                formatNum(order.cashReceived),
                formatNum(order.changeGiven),
                order.status,
                order.orderNote
            ))
        }

        return sb.toString()
    }

    /**
     * Generates Product Sales Performance Table CSV.
     */
    fun generateProductPerformanceCsv(
        orders: List<OrderEntity>,
        products: List<Product>,
        periodLabel: String
    ): String {
        val summary = computeSummary(orders, products, periodLabel)
        val sb = StringBuilder()
        sb.append("\uFEFF") // UTF-8 BOM

        sb.appendLine(csvRow(
            "SKU",
            "Nama_Produk",
            "Kategori",
            "Qty_Terjual",
            "Harga_Modal_HPP",
            "Harga_Jual_Rata2",
            "Total_Omset",
            "Total_Modal_HPP",
            "Laba_Kotor",
            "Margin_Persen"
        ))

        summary.productMetrics.forEach { p ->
            sb.appendLine(csvRow(
                p.sku,
                p.name,
                p.category,
                p.quantitySold.toString(),
                formatNum(p.unitCost),
                formatNum(p.unitPrice),
                formatNum(p.totalRevenue),
                formatNum(p.totalCost),
                formatNum(p.grossProfit),
                String.format(Locale.US, "%.2f", p.profitMarginPercent)
            ))
        }

        return sb.toString()
    }

    /**
     * Saves CSV string to app internal cache directory for sharing/file operations.
     */
    fun saveCsvToCache(context: Context, filename: String, csvContent: String): File {
        val dir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
        val file = File(dir, filename)
        FileOutputStream(file).use { output ->
            output.write(csvContent.toByteArray(Charsets.UTF_8))
        }
        return file
    }

    /**
     * Shares a CSV file using Android's system share sheet (WhatsApp, Email, Drive, etc.).
     */
    fun shareCsvFile(context: Context, file: File, title: String = "Laporan Penjualan CSV") {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "$title\nFile CSV terlampir untuk akuntansi dan analisis bisnis.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Bagikan Laporan CSV ke:")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    internal fun csvRow(vararg fields: String): String {
        return fields.joinToString(",") { escapeCsv(it) }
    }

    internal fun escapeCsv(value: String): String {
        var str = value
        val containsSpecial = str.contains(',') || str.contains('"') || str.contains('\n') || str.contains('\r')
        if (str.contains('"')) {
            str = str.replace("\"", "\"\"")
        }
        return if (containsSpecial) "\"$str\"" else str
    }

    internal fun formatNum(amount: Double): String {
        // Plain integer/decimal formatting for easy spreadsheet manipulation
        return if (amount % 1.0 == 0.0) {
            String.format(Locale.US, "%.0f", amount)
        } else {
            String.format(Locale.US, "%.2f", amount)
        }
    }

    private fun getStartOfDay(timeMillis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun getEndOfDay(timeMillis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    private class MutableProductSales(
        val sku: String,
        val name: String,
        val category: String,
        val unitCost: Double,
        var quantitySold: Int = 0,
        var totalRevenue: Double = 0.0,
        var totalCost: Double = 0.0
    )
}
