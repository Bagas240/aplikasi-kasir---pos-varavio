package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.model.BusinessType
import com.example.data.model.CartItem
import com.example.data.model.OrderEntity
import com.example.data.model.StoreProfile
import java.io.ByteArrayOutputStream

object EscPosPrinterHelper {

    // ESC/POS Command constants
    private val ESC: Byte = 0x1B
    private val GS: Byte = 0x1D
    private val LF: Byte = 0x0A

    /**
     * Formats receipt as printable monospace plain text
     */
    fun formatReceiptText(
        order: OrderEntity,
        items: List<CartItem>,
        store: StoreProfile,
        is80mm: Boolean = false,
        customReceiptNote: String = order.orderNote
    ): String {
        val width = if (is80mm) 48 else 32
        val sb = StringBuilder()

        fun center(text: String): String {
            if (text.length >= width) return text
            val pad = (width - text.length) / 2
            return " ".repeat(pad) + text
        }

        fun wrapText(text: String, maxLen: Int = width): List<String> {
            val words = text.split(" ")
            val lines = mutableListOf<String>()
            var current = StringBuilder()
            for (word in words) {
                if (current.isEmpty()) {
                    current.append(word)
                } else if (current.length + 1 + word.length <= maxLen) {
                    current.append(" ").append(word)
                } else {
                    lines.add(current.toString())
                    current = StringBuilder(word)
                }
            }
            if (current.isNotEmpty()) {
                lines.add(current.toString())
            }
            return if (lines.isEmpty()) listOf(text) else lines
        }

        fun line(): String = "-".repeat(width)
        fun doubleLine(): String = "=".repeat(width)

        fun twoCols(left: String, right: String): String {
            val space = width - left.length - right.length
            return if (space > 0) left + " ".repeat(space) + right else "$left $right"
        }

        val isOnline = store.businessType == BusinessType.ONLINE

        sb.appendLine(doubleLine())
        sb.appendLine(center(store.storeName))
        if (isOnline) {
            sb.appendLine(center("INVOICE / NOTA PESANAN ONLINE"))
            if (store.onlineStoreLink.isNotBlank()) sb.appendLine(center("Web: ${store.onlineStoreLink}"))
            if (store.instagram.isNotBlank()) sb.appendLine(center("IG: ${store.instagram}"))
            if (store.phone.isNotBlank()) sb.appendLine(center("WA: ${store.phone}"))
        } else {
            if (store.address.isNotBlank()) sb.appendLine(center(store.address))
            if (store.phone.isNotBlank()) sb.appendLine(center("Telp: ${store.phone}"))
            if (store.instagram.isNotBlank()) sb.appendLine(center(store.instagram))
        }
        sb.appendLine(doubleLine())

        if (isOnline) {
            // ONLINE SHOP: No cashier name, no walk-in customer
            sb.appendLine(twoCols("No. Pesanan :", order.orderId))
            sb.appendLine(twoCols("Waktu Order :", CurrencyFormatter.formatDate(order.timestamp)))
            // No cashier name printed on online receipt!
            val onlineRecipient = if (order.customerName.isBlank() || order.customerName.contains("Walk-In", ignoreCase = true)) {
                "Pelanggan Online"
            } else {
                order.customerName
            }
            sb.appendLine(twoCols("Penerima    :", onlineRecipient))
            if (order.customerPhone.isNotBlank()) {
                sb.appendLine(twoCols("No. WA/Telp :", order.customerPhone))
            }
            val courierName = if (order.courier.isNotBlank()) order.courier else store.defaultCourier
            if (courierName.isNotBlank()) {
                sb.appendLine(twoCols("Ekspedisi   :", courierName))
            }
            if (order.shippingAddress.isNotBlank()) {
                sb.appendLine("Alamat Kirim:")
                wrapText(order.shippingAddress).forEach { wrapped ->
                    sb.appendLine("  $wrapped")
                }
            }
        } else {
            // OFFLINE STORE: Physical cashier & shift
            sb.appendLine(twoCols("No. Struk :", order.orderId))
            sb.appendLine(twoCols("Waktu     :", CurrencyFormatter.formatDate(order.timestamp)))
            val cashierInfo = if (order.cashierRole.isNotBlank()) "${order.cashierName} (${order.cashierRole})" else order.cashierName
            sb.appendLine(twoCols("Kasir     :", cashierInfo))
            if (order.shiftName.isNotBlank()) {
                sb.appendLine(twoCols("Shift     :", order.shiftName))
            }
            sb.appendLine(twoCols("Pelanggan :", order.customerName))
        }
        sb.appendLine(line())

        items.forEach { item ->
            sb.appendLine(item.displayName)
            val qtyUnit = "${item.quantity} x ${CurrencyFormatter.formatRupiah(item.unitPrice)}"
            val itemTot = CurrencyFormatter.formatRupiah(item.totalPrice)
            sb.appendLine(twoCols("  $qtyUnit", itemTot))
            if (item.discountAmount > 0) {
                sb.appendLine(twoCols("  Disc.", "-${CurrencyFormatter.formatRupiah(item.discountAmount)}"))
            }
            if (item.itemNote.isNotBlank()) {
                sb.appendLine("  * ${item.itemNote}")
            }
        }

        sb.appendLine(line())
        sb.appendLine(twoCols("Subtotal Produk", CurrencyFormatter.formatRupiah(order.subtotal)))
        if (order.shippingFee > 0) {
            sb.appendLine(twoCols("Ongkos Kirim", CurrencyFormatter.formatRupiah(order.shippingFee)))
        }
        if (order.discountTotal > 0) {
            sb.appendLine(twoCols("Total Diskon", "-${CurrencyFormatter.formatRupiah(order.discountTotal)}"))
        }
        if (order.taxAmount > 0) {
            sb.appendLine(twoCols("PPN/Tax (${order.taxPercent.toInt()}%)", CurrencyFormatter.formatRupiah(order.taxAmount)))
        }
        if (order.serviceAmount > 0) {
            sb.appendLine(twoCols("Service (${order.servicePercent.toInt()}%)", CurrencyFormatter.formatRupiah(order.serviceAmount)))
        }
        sb.appendLine(doubleLine())
        sb.appendLine(twoCols("TOTAL BAYAR", CurrencyFormatter.formatRupiah(order.grandTotal)))
        sb.appendLine(twoCols("Metode Bayar", order.paymentMethod))

        if (order.cashReceived > 0) {
            sb.appendLine(twoCols("Tunai Diterima", CurrencyFormatter.formatRupiah(order.cashReceived)))
            sb.appendLine(twoCols("Kembalian", CurrencyFormatter.formatRupiah(order.changeGiven)))
        }

        if (order.splitAmount1 > 0 && order.splitAmount2 > 0) {
            sb.appendLine(twoCols("Split Bayar 1", CurrencyFormatter.formatRupiah(order.splitAmount1)))
            sb.appendLine(twoCols("Split Bayar 2 (${order.splitMethod2})", CurrencyFormatter.formatRupiah(order.splitAmount2)))
        }

        sb.appendLine(line())

        // Custom Personalized Message for Customer (user can type custom message)
        val effectiveNote = if (customReceiptNote.isNotBlank()) customReceiptNote else order.orderNote
        if (effectiveNote.isNotBlank()) {
            val messageHeader = if (isOnline) "💌 PESAN DARI TOKO 💌" else "💌 PESAN UNTUK PELANGGAN 💌"
            sb.appendLine(center(messageHeader))
            effectiveNote.lines().forEach { noteLine ->
                val trimmed = noteLine.trim()
                if (trimmed.isNotBlank()) {
                    wrapText(trimmed).forEach { wrappedLine ->
                        sb.appendLine(center(wrappedLine))
                    }
                }
            }
            sb.appendLine(line())
        }

        // Teks Tebal Bold Besar Semua untuk Bisnis Online:
        if (isOnline) {
            val refundNotice = "!!PERHATIAN JANGAN SAMPAI HILANG STRUK INI DAN SERTAKAN VIDEO UNBOXING KETIKA INGIN REFUND!!"
            sb.appendLine(doubleLine())
            wrapText(refundNotice).forEach { noticeLine ->
                sb.appendLine(center(noticeLine))
            }
            sb.appendLine(doubleLine())
        }

        val effectiveFooter = if (store.receiptFooter.isNotBlank()) {
            store.receiptFooter
        } else if (isOnline) {
            "Terima kasih telah berbelanja online!\nKepuasan Anda adalah prioritas kami 🙏"
        } else {
            "Terima kasih atas kunjungan Anda!\nBarang yang sudah dibeli tidak dapat ditukar."
        }

        effectiveFooter.lines().forEach { footLine ->
            sb.appendLine(center(footLine.trim()))
        }
        sb.appendLine(doubleLine())

        return sb.toString()
    }

    /**
     * Converts receipt to raw ESC/POS binary stream for Bluetooth thermal printers
     */
    fun generateEscPosBytes(
        order: OrderEntity,
        items: List<CartItem>,
        store: StoreProfile,
        is80mm: Boolean = false,
        customReceiptNote: String = order.orderNote
    ): ByteArray {
        val out = ByteArrayOutputStream()

        // Init printer
        out.write(byteArrayOf(ESC, 0x40))

        val textBody = formatReceiptText(order, items, store, is80mm, customReceiptNote)
        out.write(textBody.toByteArray(Charsets.ISO_8859_1))

        // Feed & Paper Cut (GS V 66 0)
        out.write(byteArrayOf(LF, LF, LF))
        out.write(byteArrayOf(GS, 0x56, 0x42, 0x00))

        return out.toByteArray()
    }

    /**
     * Share digital receipt via WhatsApp / Message / Email
     */
    fun shareReceipt(context: Context, receiptText: String, orderId: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Struk Transaksi #$orderId")
            putExtra(Intent.EXTRA_TEXT, receiptText)
        }
        context.startActivity(Intent.createChooser(intent, "Kirim Struk Digital"))
    }
}
