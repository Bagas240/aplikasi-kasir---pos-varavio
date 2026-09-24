package com.example.util

import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.data.model.Product
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * Result data class representing a scanned barcode and its mapped inventory product.
 */
data class BarcodeScanResult(
    val rawCode: String,
    val format: Int = Barcode.FORMAT_UNKNOWN,
    val formatName: String = "Unknown",
    val matchedProduct: Product? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isMapped: Boolean get() = matchedProduct != null
}

/**
 * Core matching logic that maps scanned barcode / SKU strings to existing inventory products.
 * Handles exact matches, case-insensitivity, leading zeroes (UPC-A vs EAN-13),
 * and alphanumeric SKU variants.
 */
object BarcodeItemMatcher {

    /**
     * Finds a single matching product from the current inventory.
     */
    fun matchProduct(scannedCode: String, inventory: List<Product>): Product? {
        val trimmed = scannedCode.trim()
        if (trimmed.isEmpty()) return null

        // 1. Exact barcode match
        val exactBarcode = inventory.firstOrNull { it.barcode.trim() == trimmed }
        if (exactBarcode != null) return exactBarcode

        // 2. Exact SKU match
        val exactSku = inventory.firstOrNull { it.sku.trim() == trimmed }
        if (exactSku != null) return exactSku

        // 3. Case-insensitive barcode match
        val caseInsensitiveBarcode = inventory.firstOrNull {
            it.barcode.trim().equals(trimmed, ignoreCase = true)
        }
        if (caseInsensitiveBarcode != null) return caseInsensitiveBarcode

        // 4. Case-insensitive SKU match
        val caseInsensitiveSku = inventory.firstOrNull {
            it.sku.trim().equals(trimmed, ignoreCase = true)
        }
        if (caseInsensitiveSku != null) return caseInsensitiveSku

        // 5. Normalized alphanumeric match (strip dashes, spaces, slashes)
        val sanitizedQuery = sanitizeCode(trimmed)
        if (sanitizedQuery.isNotEmpty()) {
            val sanitizedMatch = inventory.firstOrNull {
                sanitizeCode(it.barcode) == sanitizedQuery || sanitizeCode(it.sku) == sanitizedQuery
            }
            if (sanitizedMatch != null) return sanitizedMatch
        }

        // 6. EAN-13 vs UPC-A cross-format compatibility
        // EAN-13 with leading zero (e.g. 0012345678905) matches UPC-A (012345678905)
        if (trimmed.length == 13 && trimmed.startsWith("0")) {
            val withoutLeadingZero = trimmed.drop(1)
            val upcMatch = inventory.firstOrNull {
                it.barcode.trim() == withoutLeadingZero || it.sku.trim() == withoutLeadingZero
            }
            if (upcMatch != null) return upcMatch
        }

        // UPC-A (12 digits) matching EAN-13 stored with leading zero
        if (trimmed.length == 12) {
            val withLeadingZero = "0$trimmed"
            val eanMatch = inventory.firstOrNull {
                it.barcode.trim() == withLeadingZero || it.sku.trim() == withLeadingZero
            }
            if (eanMatch != null) return eanMatch
        }

        return null
    }

    /**
     * Filters inventory for all products matching or partially containing the scanned code.
     */
    fun searchProductsByCode(query: String, inventory: List<Product>): List<Product> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return inventory

        val directMatch = matchProduct(trimmed, inventory)
        val partials = inventory.filter { prod ->
            prod.barcode.contains(trimmed, ignoreCase = true) ||
                    prod.sku.contains(trimmed, ignoreCase = true) ||
                    prod.name.contains(trimmed, ignoreCase = true)
        }

        return if (directMatch != null) {
            listOf(directMatch) + partials.filterNot { it.id == directMatch.id }
        } else {
            partials
        }
    }

    private fun sanitizeCode(input: String): String {
        return input.filter { it.isLetterOrDigit() }.uppercase()
    }

    fun getBarcodeFormatName(format: Int): String {
        return when (format) {
            Barcode.FORMAT_EAN_13 -> "EAN-13"
            Barcode.FORMAT_EAN_8 -> "EAN-8"
            Barcode.FORMAT_UPC_A -> "UPC-A"
            Barcode.FORMAT_UPC_E -> "UPC-E"
            Barcode.FORMAT_CODE_128 -> "Code 128"
            Barcode.FORMAT_CODE_39 -> "Code 39"
            Barcode.FORMAT_CODE_93 -> "Code 93"
            Barcode.FORMAT_QR_CODE -> "QR Code"
            Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
            Barcode.FORMAT_ITF -> "ITF"
            Barcode.FORMAT_CODABAR -> "Codabar"
            Barcode.FORMAT_AZTEC -> "Aztec"
            Barcode.FORMAT_PDF417 -> "PDF417"
            else -> "Barcode"
        }
    }
}

/**
 * CameraX ImageAnalysis.Analyzer that runs Google ML Kit Barcode Scanning on incoming frames.
 * Uses configurable debouncing and mapping against the active product inventory.
 */
class MlKitBarcodeAnalyzer(
    private val inventoryProvider: () -> List<Product>,
    private val debounceMillis: Long = 1200L,
    private val onBarcodeResult: (BarcodeScanResult) -> Unit
) : ImageAnalysis.Analyzer {

    private val barcodeScanner: BarcodeScanner by lazy {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_ALL_FORMATS
            )
            .build()
        BarcodeScanning.getClient(options)
    }

    private var lastScannedRawCode: String = ""
    private var lastScannedTimestamp: Long = 0L

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue?.trim() ?: continue
                    if (rawValue.isBlank()) continue

                    val now = System.currentTimeMillis()
                    // Debounce: allow scan if code is different or debounce duration has elapsed
                    if (rawValue != lastScannedRawCode || now - lastScannedTimestamp > debounceMillis) {
                        lastScannedRawCode = rawValue
                        lastScannedTimestamp = now

                        val currentInventory = inventoryProvider()
                        val matchedProduct = BarcodeItemMatcher.matchProduct(rawValue, currentInventory)
                        val formatName = BarcodeItemMatcher.getBarcodeFormatName(barcode.format)

                        val result = BarcodeScanResult(
                            rawCode = rawValue,
                            format = barcode.format,
                            formatName = formatName,
                            matchedProduct = matchedProduct,
                            timestamp = now
                        )
                        onBarcodeResult(result)
                        break // Process first recognized barcode per frame
                    }
                }
            }
            .addOnFailureListener {
                // Ignore transient frame analysis errors
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
