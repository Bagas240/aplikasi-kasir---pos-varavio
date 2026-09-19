package com.example.util

import android.content.Context
import android.content.Intent
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
    fun formatReceiptText(order: OrderEntity, items: List<CartItem>, store: StoreProfile, is80mm: Boolean = false): String {
        val width = if (is80mm) 48 else 32
        val sb = StringBuilder()

        fun center(text: String): String {
            if (text.length >= width) return text.take(width)
            val pad = (width - text.length) / 2
            return " ".repeat(pad) + text
        }

        fun line(): String = "-".repeat(width)
        fun doubleLine(): String = "=".repeat(width)

        fun twoCols(left: String, right: String): String {
            val space = width - left.length - right.length
            return if (space > 0) left + " ".repeat(space) + right else "$left $right"
        }

        sb.appendLine(doubleLine())
        sb.appendLine(center(store.storeName))
        sb.appendLine(center(store.address))
        sb.appendLine(center("Telp: ${store.phone}"))
        if (store.instagram.isNotBlank()) sb.appendLine(center(store.instagram))
        sb.appendLine(doubleLine())

        sb.appendLine(twoCols("No. Struk :", order.orderId))
        sb.appendLine(twoCols("Waktu     :", CurrencyFormatter.formatDate(order.timestamp)))
        sb.appendLine(twoCols("Kasir     :", order.cashierName))
        sb.appendLine(twoCols("Pelanggan :", order.customerName))
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
        sb.appendLine(twoCols("Subtotal", CurrencyFormatter.formatRupiah(order.subtotal)))
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
        sb.appendLine(twoCols("TOTAL", CurrencyFormatter.formatRupiah(order.grandTotal)))
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
        store.receiptFooter.lines().forEach { footLine ->
            sb.appendLine(center(footLine.trim()))
        }
        sb.appendLine(doubleLine())

        return sb.toString()
    }

    /**
     * Converts receipt to raw ESC/POS binary stream for Bluetooth thermal printers
     */
    fun generateEscPosBytes(order: OrderEntity, items: List<CartItem>, store: StoreProfile, is80mm: Boolean = false): ByteArray {
        val out = ByteArrayOutputStream()

        // Init printer
        out.write(byteArrayOf(ESC, 0x40))

        val textBody = formatReceiptText(order, items, store, is80mm)
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
