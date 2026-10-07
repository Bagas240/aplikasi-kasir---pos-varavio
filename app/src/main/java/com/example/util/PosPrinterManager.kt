package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.print.pdf.PrintedPdfDocument
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.CartItem
import com.example.data.model.OrderEntity
import com.example.data.model.Product
import com.example.data.model.StoreProfile
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object PosPrinterManager {

    /**
     * Prints an Android Bitmap directly using Android's native PrintManager.
     * This opens the system print dialogue supporting all connected printers (WiFi, Bluetooth, USB, Cloud)
     * as well as native "Save as PDF" (Simpan sebagai PDF).
     */
    fun printBitmap(context: Context, jobName: String, bitmap: Bitmap) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            Toast.makeText(context, "Layanan cetak tidak tersedia di perangkat ini", Toast.LENGTH_SHORT).show()
            return
        }

        val printAdapter = object : PrintDocumentAdapter() {
            private var pdfDocument: PrintedPdfDocument? = null

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }

                pdfDocument = PrintedPdfDocument(context, newAttributes)

                val info = PrintDocumentInfo.Builder("$jobName.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()

                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onWriteCancelled()
                    return
                }

                val doc = pdfDocument ?: return
                val page = doc.startPage(0)

                val canvas = page.canvas
                val pageWidth = page.info.pageWidth
                val pageHeight = page.info.pageHeight

                // Scale bitmap to fit page width while preserving aspect ratio
                val scale = pageWidth.toFloat() / bitmap.width.toFloat()
                val targetHeight = (bitmap.height * scale).toInt()

                val destRect = Rect(0, 0, pageWidth, minOf(pageHeight, targetHeight))
                canvas.drawBitmap(bitmap, null, destRect, null)

                doc.finishPage(page)

                try {
                    val out: OutputStream = FileOutputStream(destination?.fileDescriptor)
                    doc.writeTo(out)
                    out.close()
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                } finally {
                    doc.close()
                }
            }
        }

        val attributes = PrintAttributes.Builder()
            .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()

        printManager.print(jobName, printAdapter, attributes)
    }

    /**
     * Saves a Bitmap directly to the device's Gallery or Downloads directory
     * and shows a toast. Also allows launching an intent to view/share.
     */
    fun saveBitmapToDevice(context: Context, bitmap: Bitmap, fileNamePrefix: String): Uri? {
        val fileName = "${fileNamePrefix}_${System.currentTimeMillis()}.png"
        var savedUri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/VoravioPOS")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    }
                    savedUri = uri
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val posDir = File(imagesDir, "VoravioPOS").apply { if (!exists()) mkdirs() }
                val imageFile = File(posDir, fileName)
                FileOutputStream(imageFile).use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
                savedUri = Uri.fromFile(imageFile)
            }

            // Fallback internal cache if external failed
            if (savedUri == null) {
                val cacheDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
                val cacheFile = File(cacheDir, fileName)
                FileOutputStream(cacheFile).use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
                savedUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cacheFile)
            }

            Toast.makeText(context, "Berhasil diunduh! File tersimpan: $fileName", Toast.LENGTH_LONG).show()

            // Open share / view chooser so user can easily open or send the file
            if (savedUri != null) {
                try {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, savedUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooser = Intent.createChooser(shareIntent, "Buka / Bagikan File Label").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(chooser)
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal mengunduh file: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        return savedUri
    }

    /**
     * Renders a complete high-resolution receipt as an Android Bitmap
     * suitable for printing on standard 58mm/80mm thermal printers or saving as an image.
     */
    fun renderReceiptBitmap(
        context: Context,
        order: OrderEntity,
        items: List<CartItem>,
        store: StoreProfile,
        is80mm: Boolean = false,
        customReceiptNote: String = order.orderNote
    ): Bitmap {
        val widthPx = if (is80mm) 576 else 384 // standard ESC/POS pixel widths
        val padding = 16
        val contentWidth = widthPx - (padding * 2)

        // Monospace & Sans fonts
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 14f
            typeface = Typeface.MONOSPACE
        }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 14f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        val totalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GRAY
            strokeWidth = 1.5f
        }

        // Estimate canvas height
        val effectiveNote = if (customReceiptNote.isNotBlank()) customReceiptNote else order.orderNote
        val noteLinesCount = if (effectiveNote.isNotBlank()) effectiveNote.lines().size + 2 else 0
        val estimatedLines = 26 + (items.size * 3) + store.receiptFooter.lines().size + noteLinesCount
        val heightPx = estimatedLines * 24 + 120

        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        var y = 30f
        val centerX = widthPx / 2f

        // 1. Store Header
        canvas.drawText(store.storeName.uppercase(), centerX, y, titlePaint)
        y += 20f
        if (store.address.isNotBlank()) {
            canvas.drawText(store.address, centerX, y, headerPaint)
            y += 18f
        }
        if (store.phone.isNotBlank()) {
            canvas.drawText("Telp: ${store.phone}", centerX, y, headerPaint)
            y += 18f
        }
        if (store.instagram.isNotBlank()) {
            canvas.drawText(store.instagram, centerX, y, headerPaint)
            y += 18f
        }

        y += 6f
        canvas.drawLine(padding.toFloat(), y, (widthPx - padding).toFloat(), y, linePaint)
        y += 18f

        // 2. Metadata (Order ID, Waktu, Kasir, Pelanggan)
        fun drawTwoColumns(left: String, right: String, paint: Paint = textPaint) {
            canvas.drawText(left, padding.toFloat(), y, paint)
            val rightWidth = paint.measureText(right)
            canvas.drawText(right, (widthPx - padding - rightWidth), y, paint)
            y += 18f
        }

        drawTwoColumns("No. Struk :", order.orderId)
        drawTwoColumns("Waktu     :", CurrencyFormatter.formatDate(order.timestamp))
        val cashierInfo = if (order.cashierRole.isNotBlank()) "${order.cashierName} (${order.cashierRole})" else order.cashierName
        drawTwoColumns("Kasir     :", cashierInfo)
        if (order.shiftName.isNotBlank()) {
            drawTwoColumns("Shift     :", order.shiftName)
        }
        drawTwoColumns("Pelanggan :", order.customerName)

        y += 4f
        canvas.drawLine(padding.toFloat(), y, (widthPx - padding).toFloat(), y, linePaint)
        y += 18f

        // 3. Items list
        items.forEach { item ->
            canvas.drawText(item.displayName.take(30), padding.toFloat(), y, boldPaint)
            y += 16f
            val qtyRate = "${item.quantity} x ${CurrencyFormatter.formatRupiah(item.unitPrice).replace("Rp ", "")}"
            val totalStr = CurrencyFormatter.formatRupiah(item.totalPrice)
            drawTwoColumns("  $qtyRate", totalStr)
            if (item.discountAmount > 0) {
                drawTwoColumns("  Diskon", "-${CurrencyFormatter.formatRupiah(item.discountAmount)}")
            }
            if (item.itemNote.isNotBlank()) {
                canvas.drawText("  * ${item.itemNote}", padding.toFloat(), y, headerPaint)
                y += 16f
            }
        }

        y += 4f
        canvas.drawLine(padding.toFloat(), y, (widthPx - padding).toFloat(), y, linePaint)
        y += 18f

        // 4. Totals
        drawTwoColumns("Subtotal", CurrencyFormatter.formatRupiah(order.subtotal))
        if (order.discountTotal > 0) {
            drawTwoColumns("Total Diskon", "-${CurrencyFormatter.formatRupiah(order.discountTotal)}")
        }
        if (order.taxAmount > 0) {
            drawTwoColumns("PPN (${order.taxPercent.toInt()}%)", CurrencyFormatter.formatRupiah(order.taxAmount))
        }
        if (order.serviceAmount > 0) {
            drawTwoColumns("Layanan (${order.servicePercent.toInt()}%)", CurrencyFormatter.formatRupiah(order.serviceAmount))
        }

        y += 4f
        canvas.drawLine(padding.toFloat(), y, (widthPx - padding).toFloat(), y, linePaint)
        y += 22f

        // Grand Total Bold
        canvas.drawText("TOTAL", padding.toFloat(), y, totalPaint)
        val grandTotalStr = CurrencyFormatter.formatRupiah(order.grandTotal)
        val grandTotalWidth = totalPaint.measureText(grandTotalStr)
        canvas.drawText(grandTotalStr, (widthPx - padding - grandTotalWidth), y, totalPaint)
        y += 24f

        drawTwoColumns("Metode Bayar", order.paymentMethod)
        if (order.cashReceived > 0) {
            drawTwoColumns("Tunai Diterima", CurrencyFormatter.formatRupiah(order.cashReceived))
            drawTwoColumns("Kembalian", CurrencyFormatter.formatRupiah(order.changeGiven))
        }

        y += 8f
        canvas.drawLine(padding.toFloat(), y, (widthPx - padding).toFloat(), y, linePaint)
        y += 20f

        // 5. Custom Personalized Message for Customer
        if (effectiveNote.isNotBlank()) {
            val noteHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val noteBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1E293B")
                textSize = 13f
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
            }

            canvas.drawText("💌 PESAN UNTUK PELANGGAN 💌", centerX, y, noteHeaderPaint)
            y += 18f
            effectiveNote.lines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotBlank()) {
                    canvas.drawText("\"$trimmed\"", centerX, y, noteBodyPaint)
                    y += 18f
                }
            }
            y += 4f
            canvas.drawLine(padding.toFloat(), y, (widthPx - padding).toFloat(), y, linePaint)
            y += 20f
        }

        // 6. Store Footer Notes
        store.receiptFooter.lines().forEach { line ->
            canvas.drawText(line.trim(), centerX, y, headerPaint)
            y += 16f
        }

        // Crop bitmap to actual drawn height
        val finalHeight = (y + 20).toInt().coerceAtMost(heightPx)
        return Bitmap.createBitmap(bitmap, 0, 0, widthPx, finalHeight)
    }

    /**
     * Renders a barcode label tag as an Android Bitmap
     * suitable for printing on label printers (e.g. 58mm/80mm) or downloading.
     */
    fun renderBarcodeLabelBitmap(
        context: Context,
        store: StoreProfile,
        product: Product,
        barcodeString: String,
        format: BarcodeFormat,
        is80mm: Boolean = false
    ): Bitmap {
        val widthPx = if (is80mm) 500 else 380
        val heightPx = if (format == BarcodeFormat.QR_CODE) 340 else 240
        val padding = 16

        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRect(4f, 4f, widthPx - 4f, heightPx - 4f, borderPaint)

        val storePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF1E40AF.toInt()
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF0F172A.toInt()
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val pricePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF059669.toInt() // Emerald
            textSize = 18f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val codeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF475569.toInt()
            textSize = 13f
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
        }

        val centerX = widthPx / 2f
        var y = 26f

        // 1. Store Header
        canvas.drawText(store.storeName.uppercase(), centerX, y, storePaint)
        y += 22f

        // 2. Product Name
        canvas.drawText(product.name.take(28), centerX, y, namePaint)
        y += 24f

        // 3. Product Price
        canvas.drawText(CurrencyFormatter.formatRupiah(product.sellPrice), centerX, y, pricePaint)
        y += 12f

        // 4. Render ZXing Barcode/QR
        val codeToEncode = barcodeString.ifBlank { product.barcode.ifBlank { product.sku } }
        val barcodeBitmap = if (format == BarcodeFormat.QR_CODE) {
            BarcodeGenerator.generateBarcodeBitmapWithZxing(
                contents = codeToEncode,
                format = com.google.zxing.BarcodeFormat.QR_CODE,
                width = 160,
                height = 160
            )
        } else {
            val zFormat = if (format == BarcodeFormat.EAN_13) com.google.zxing.BarcodeFormat.EAN_13 else com.google.zxing.BarcodeFormat.CODE_128
            BarcodeGenerator.generateBarcodeBitmapWithZxing(
                contents = codeToEncode,
                format = zFormat,
                width = widthPx - (padding * 2),
                height = 80
            )
        }

        val destLeft = (widthPx - barcodeBitmap.width) / 2f
        canvas.drawBitmap(barcodeBitmap, destLeft, y, null)
        y += barcodeBitmap.height + 16f

        // 5. Code digits below barcode
        canvas.drawText(codeToEncode, centerX, y, codeTextPaint)

        return bitmap
    }

    /**
     * One-step Print Barcode Label using Android System Print Dialog
     */
    fun printBarcodeLabelToSystem(
        context: Context,
        store: StoreProfile,
        product: Product,
        barcodeString: String,
        format: BarcodeFormat,
        is80mm: Boolean = false
    ) {
        val bitmap = renderBarcodeLabelBitmap(context, store, product, barcodeString, format, is80mm)
        printBitmap(context, "Barcode_${product.sku}", bitmap)
    }

    /**
     * One-step Download Barcode Label to device storage
     */
    fun downloadBarcodeLabel(
        context: Context,
        store: StoreProfile,
        product: Product,
        barcodeString: String,
        format: BarcodeFormat,
        is80mm: Boolean = false
    ): Uri? {
        val bitmap = renderBarcodeLabelBitmap(context, store, product, barcodeString, format, is80mm)
        return saveBitmapToDevice(context, bitmap, "barcode_${product.sku}")
    }

    /**
     * One-step Print Receipt using Android System Print Dialog
     */
    fun printReceiptToSystem(
        context: Context,
        order: OrderEntity,
        items: List<CartItem>,
        store: StoreProfile,
        is80mm: Boolean = false,
        customReceiptNote: String = order.orderNote
    ) {
        val bitmap = renderReceiptBitmap(context, order, items, store, is80mm, customReceiptNote)
        printBitmap(context, "Struk_${order.orderId}", bitmap)
    }

    /**
     * One-step Download Receipt to device storage
     */
    fun downloadReceipt(
        context: Context,
        order: OrderEntity,
        items: List<CartItem>,
        store: StoreProfile,
        is80mm: Boolean = false,
        customReceiptNote: String = order.orderNote
    ): Uri? {
        val bitmap = renderReceiptBitmap(context, order, items, store, is80mm, customReceiptNote)
        return saveBitmapToDevice(context, bitmap, "struk_${order.orderId}")
    }
}
