package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import com.example.data.model.Product
import com.example.data.model.StoreProfile
import java.io.ByteArrayOutputStream
import java.util.Random

/**
 * Service responsible for generating, validating, and encoding EAN-13 and Code 128 barcode strings.
 * Generates both alphanumeric string identifiers, check digits, and module bit patterns (BooleanArray / BitString)
 * for high-precision vector rendering on Canvas or thermal receipt printers.
 */
class BarcodeGeneratorService(
    private val random: Random = Random()
) {

    companion object {
        val instance by lazy { BarcodeGeneratorService() }

        // Code128 pattern table (Value 0 to 106)
        // Each string contains widths of 3 bars and 3 spaces (sum = 11 modules), Stop is 13 modules
        private val CODE128_PATTERNS = arrayOf(
            "212222", "222122", "222221", "121223", "121322", "131222", "122213", "122312", "132212", "221213", // 0-9
            "221312", "231212", "112232", "122132", "122231", "113222", "123122", "123221", "223211", "221132", // 10-19
            "221231", "213212", "223112", "312131", "311222", "321122", "321221", "312212", "322112", "322211", // 20-29
            "212123", "212321", "232121", "111323", "131123", "131321", "112313", "132113", "132311", "211313", // 30-39
            "231113", "231311", "112133", "112331", "132131", "113123", "113321", "133121", "313121", "211331", // 40-49
            "231131", "213113", "213311", "213131", "311123", "311321", "331121", "312113", "312311", "332111", // 50-59
            "314111", "221411", "431111", "111224", "111422", "121124", "121421", "141122", "141221", "112214", // 60-69
            "112412", "122114", "122411", "142112", "142211", "241211", "221114", "413111", "241112", "134111", // 70-79
            "111242", "121142", "121241", "114212", "124112", "124211", "411212", "421112", "421211", "212141", // 80-89
            "214121", "412121", "111143", "111341", "131141", "114113", "114311", "411113", "411311", "113141", // 90-99
            "114131", "311141", "411131", "211412", "211214", "211232", "2331112" // 100-106 (106 is STOP)
        )

        private const val CODE128_START_B = 104
        private const val CODE128_STOP = 106

        // EAN-13 Digit Patterns
        private val EAN_L = arrayOf(
            "0001101", "0011001", "0010011", "0111101", "0100011",
            "0110001", "0101111", "0111011", "0110111", "0001011"
        )
        private val EAN_G = arrayOf(
            "0100111", "0110011", "0011011", "0100001", "0011101",
            "0111001", "0000101", "0010001", "0001001", "0010111"
        )
        private val EAN_R = arrayOf(
            "1110010", "1100110", "1101100", "1000010", "1011100",
            "1001110", "1010000", "1000100", "1001000", "1110100"
        )
        private val EAN_PARITY = arrayOf(
            "LLLLLL", "LLGLGG", "LLGGLG", "LLGGGL", "LGLLGG",
            "LGGLLG", "LGGGLL", "LGLGLG", "LGLGGL", "LGGLGL"
        )
    }

    // ==========================================
    // EAN-13 GENERATION & VALIDATION
    // ==========================================

    /**
     * Generates a complete 13-digit EAN-13 barcode string.
     * @param prefix Standard country or custom enterprise prefix (default "899" for GS1 Indonesia retail).
     * @return 13-digit string ending with the mathematically calculated check digit.
     */
    fun generateEan13(prefix: String = "899"): String {
        val cleanPrefix = prefix.filter { it.isDigit() }.ifBlank { "899" }
        val remainingLength = 12 - cleanPrefix.length
        val randomDigits = buildString {
            append(cleanPrefix)
            repeat(remainingLength.coerceAtLeast(0)) {
                append(random.nextInt(10))
            }
        }.take(12).padStart(12, '0')

        val checkDigit = calculateEan13CheckDigit(randomDigits)
        return randomDigits + checkDigit
    }

    /**
     * Generates a 13-digit EAN-13 string from 12 provided digits by calculating and appending the check digit.
     * If more than 12 digits are passed, the first 12 digits are used.
     */
    fun generateEan13FromDigits(digitsInput: String): String {
        val digitsOnly = digitsInput.filter { it.isDigit() }
        val twelveDigits = when {
            digitsOnly.length < 12 -> digitsOnly.padStart(12, '0')
            digitsOnly.length > 12 -> digitsOnly.substring(0, 12)
            else -> digitsOnly
        }
        val checkDigit = calculateEan13CheckDigit(twelveDigits)
        return twelveDigits + checkDigit
    }

    /**
     * Calculates the official modulo 10 EAN-13 check digit for 12 digits.
     * Weight 1 for odd positions (index 0, 2, 4...) and weight 3 for even positions (index 1, 3, 5...).
     */
    fun calculateEan13CheckDigit(twelveDigits: String): Int {
        val clean = twelveDigits.filter { it.isDigit() }.padStart(12, '0').take(12)
        var sum = 0
        for (i in 0 until 12) {
            val digit = clean[i].digitToInt()
            sum += if (i % 2 == 0) digit else digit * 3
        }
        return (10 - (sum % 10)) % 10
    }

    /**
     * Validates whether a barcode string is a valid 13-digit EAN-13 with matching check digit.
     */
    fun validateEan13(code: String): Boolean {
        val digitsOnly = code.filter { it.isDigit() }
        if (digitsOnly.length != 13) return false
        val twelve = digitsOnly.substring(0, 12)
        val expectedCheck = calculateEan13CheckDigit(twelve)
        return digitsOnly[12].digitToInt() == expectedCheck
    }

    /**
     * Formats an EAN-13 string into the standard human-readable display grouping (e.g. "8 992753 210015").
     */
    fun formatEan13HumanReadable(ean13: String): String {
        val digits = ean13.filter { it.isDigit() }
        return if (digits.length == 13) {
            "${digits[0]} ${digits.substring(1, 7)} ${digits.substring(7, 13)}"
        } else {
            ean13
        }
    }

    /**
     * Encodes an EAN-13 code into a BooleanArray of modules (true = black bar, false = white space).
     * Total length is 113 modules (including 9 modules left quiet zone and 9 modules right quiet zone).
     */
    fun encodeEan13(digitsInput: String): BooleanArray {
        var rawDigits = digitsInput.filter { it.isDigit() }
        if (rawDigits.length < 12) {
            rawDigits = rawDigits.padStart(12, '0')
        }
        val final13 = if (rawDigits.length == 13 && validateEan13(rawDigits)) {
            rawDigits
        } else {
            generateEan13FromDigits(rawDigits.take(12))
        }

        val firstDigit = final13[0].digitToInt()
        val parityScheme = EAN_PARITY[firstDigit]

        val moduleList = ArrayList<Boolean>(113)
        // Quiet zone (9 modules)
        repeat(9) { moduleList.add(false) }

        // Start guard: 101
        moduleList.add(true)
        moduleList.add(false)
        moduleList.add(true)

        // Left 6 digits
        for (i in 1..6) {
            val d = final13[i].digitToInt()
            val pattern = if (parityScheme[i - 1] == 'L') EAN_L[d] else EAN_G[d]
            pattern.forEach { moduleList.add(it == '1') }
        }

        // Center guard: 01010
        moduleList.add(false)
        moduleList.add(true)
        moduleList.add(false)
        moduleList.add(true)
        moduleList.add(false)

        // Right 6 digits
        for (i in 7..12) {
            val d = final13[i].digitToInt()
            val pattern = EAN_R[d]
            pattern.forEach { moduleList.add(it == '1') }
        }

        // End guard: 101
        moduleList.add(true)
        moduleList.add(false)
        moduleList.add(true)

        // Right quiet zone (9 modules)
        repeat(9) { moduleList.add(false) }

        return moduleList.toBooleanArray()
    }

    /**
     * Encodes EAN-13 into a binary bit string ("1" for bar, "0" for space).
     */
    fun encodeEan13BitString(code: String): String {
        val modules = encodeEan13(code)
        return buildString(modules.size) {
            modules.forEach { append(if (it) '1' else '0') }
        }
    }

    // ==========================================
    // CODE 128 GENERATION & VALIDATION
    // ==========================================

    /**
     * Generates a Code 128 compliant alphanumeric barcode string.
     * @param content Optional desired content; if blank or null, an enterprise SKU/item code is generated.
     * @param prefix Prefix for auto-generated codes (e.g., "PRD").
     */
    fun generateCode128(content: String? = null, prefix: String = "PRD"): String {
        if (!content.isNullOrBlank()) {
            val sanitized = sanitizeCode128(content)
            if (sanitized.isNotEmpty()) return sanitized
        }
        val randomNum = (100000 + random.nextInt(900000)).toString()
        return "$prefix-$randomNum"
    }

    /**
     * Sanitizes input to valid Code 128B ASCII characters (ASCII 32 to 126).
     */
    fun sanitizeCode128(text: String): String {
        return text.filter { it.code in 32..126 }
    }

    /**
     * Validates whether a text string contains only valid Code 128B characters.
     */
    fun validateCode128(text: String): Boolean {
        if (text.isEmpty()) return false
        return text.all { it.code in 32..126 }
    }

    /**
     * Calculates the Code 128 Checksum digit (modulo 103).
     * Checksum = (StartB + Sum(CharVal * Position)) % 103
     */
    fun calculateCode128CheckDigit(text: String): Int {
        val clean = sanitizeCode128(text).ifEmpty { " " }
        var checkSum = CODE128_START_B
        clean.forEachIndexed { index, char ->
            val codeVal = char.code - 32
            checkSum += codeVal * (index + 1)
        }
        return checkSum % 103
    }

    /**
     * Encodes a string into a BooleanArray of modules using Code 128B.
     * Total modules = 10 (left quiet) + 11 (start) + 11 * length + 11 (checksum) + 13 (stop) + 10 (right quiet).
     */
    fun encodeCode128(text: String): BooleanArray {
        val cleanText = sanitizeCode128(text).ifEmpty { "000000" }
        val values = mutableListOf<Int>()
        values.add(CODE128_START_B)

        var checkSum = CODE128_START_B
        cleanText.forEachIndexed { index, char ->
            val codeVal = char.code - 32
            values.add(codeVal)
            checkSum += codeVal * (index + 1)
        }
        val checkDigit = checkSum % 103
        values.add(checkDigit)
        values.add(CODE128_STOP)

        val moduleList = ArrayList<Boolean>()
        // Left quiet zone: 10 modules
        repeat(10) { moduleList.add(false) }

        values.forEach { valIdx ->
            val pattern = CODE128_PATTERNS[valIdx]
            var isBar = true
            pattern.forEach { charDigit ->
                val count = charDigit.digitToInt()
                repeat(count) { moduleList.add(isBar) }
                isBar = !isBar
            }
        }

        // Right quiet zone: 10 modules
        repeat(10) { moduleList.add(false) }
        return moduleList.toBooleanArray()
    }

    /**
     * Encodes Code 128 into a binary bit string ("1" for bar, "0" for space).
     */
    fun encodeCode128BitString(text: String): String {
        val modules = encodeCode128(text)
        return buildString(modules.size) {
            modules.forEach { append(if (it) '1' else '0') }
        }
    }

    // ==========================================
    // UNIFIED HELPER METHODS
    // ==========================================

    /**
     * Formats barcode text for clean human display based on format.
     */
    fun formatHumanReadable(code: String, format: BarcodeFormat): String {
        return when (format) {
            BarcodeFormat.EAN_13 -> formatEan13HumanReadable(code)
            BarcodeFormat.CODE_128 -> code
            BarcodeFormat.QR_CODE -> code
        }
    }

    /**
     * Encodes according to the chosen format.
     */
    fun encode(code: String, format: BarcodeFormat): BooleanArray {
        return when (format) {
            BarcodeFormat.EAN_13 -> encodeEan13(code)
            BarcodeFormat.CODE_128 -> encodeCode128(code)
            BarcodeFormat.QR_CODE -> BooleanArray(0)
        }
    }

    // ==========================================
    // PRINT ENGINE INTEGRATION SUPPORT
    // ==========================================

    /**
     * Converts a 1D barcode boolean array into an Android Bitmap for image printing or preview.
     */
    fun createBarcodeBitmap(modules: BooleanArray, width: Int = 400, height: Int = 120): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        if (modules.isEmpty()) return bitmap
        val moduleWidth = width.toFloat() / modules.size

        for (x in 0 until width) {
            val moduleIdx = (x / moduleWidth).toInt().coerceIn(0, modules.size - 1)
            val isBlack = modules[moduleIdx]
            val color = if (isBlack) Color.BLACK else Color.WHITE
            for (y in 0 until height) {
                bitmap.setPixel(x, y, color)
            }
        }
        return bitmap
    }

    /**
     * Generates a high-resolution barcode bitmap image from product ID using ZXing,
     * specially tailored for printing labels for items that don't have existing manufacturer codes.
     */
    fun generateBarcodeBitmapFromProductId(
        productId: Long,
        format: BarcodeFormat = BarcodeFormat.CODE_128,
        width: Int = 480,
        height: Int = 160,
        customPrefix: String = "PRD"
    ): Bitmap {
        return BarcodeGenerator.generateBarcodeBitmapFromProductId(
            productId = productId,
            format = format,
            width = width,
            height = height,
            customPrefix = customPrefix
        )
    }

    /**
     * Generates standard barcode string from product ID for internal store labeling.
     */
    fun generateBarcodeStringFromProductId(
        productId: Long,
        format: BarcodeFormat = BarcodeFormat.CODE_128,
        customPrefix: String = "PRD"
    ): String {
        return BarcodeGenerator.generateBarcodeStringFromProductId(
            productId = productId,
            format = format,
            customPrefix = customPrefix
        )
    }

    /**
     * Generates native ESC/POS barcode command stream (GS k) for Bluetooth thermal receipt printers.
     * Compatible with 58mm and 80mm ESC/POS hardware print engines.
     */
    fun generateEscPosNativeBarcode(
        code: String,
        format: BarcodeFormat,
        height: Int = 64,
        width: Int = 2,
        showHri: Boolean = true
    ): ByteArray {
        val out = ByteArrayOutputStream()
        val GS: Byte = 0x1D
        val ESC: Byte = 0x1B

        // Center align
        out.write(byteArrayOf(ESC, 0x61, 0x01))

        // Set barcode height (GS h n: 1 <= n <= 255)
        out.write(byteArrayOf(GS, 0x68, height.coerceIn(20, 255).toByte()))

        // Set barcode module width (GS w n: 2 <= n <= 6)
        out.write(byteArrayOf(GS, 0x77, width.coerceIn(1, 6).toByte()))

        // Set HRI characters print position (GS H n: 0=none, 2=below)
        val hriPos: Byte = if (showHri) 0x02 else 0x00
        out.write(byteArrayOf(GS, 0x48, hriPos))

        when (format) {
            BarcodeFormat.EAN_13 -> {
                val clean13 = if (code.length == 13 && validateEan13(code)) {
                    code
                } else {
                    generateEan13FromDigits(code)
                }
                // ESC/POS EAN13 Format B: GS k 67 n d1...dn (n = 12 or 13)
                val bytes = clean13.toByteArray(Charsets.ISO_8859_1)
                out.write(byteArrayOf(GS, 0x6B, 0x43, bytes.size.toByte()))
                out.write(bytes)
            }
            BarcodeFormat.CODE_128 -> {
                val sanitized = sanitizeCode128(code).ifEmpty { "PRD-000001" }
                // ESC/POS Code128 Format B: GS k 73 n {B d1...dn
                // Prefix with "{B" (0x7B, 0x42) for Code 128 Set B
                val code128Payload = "{B$sanitized".toByteArray(Charsets.ISO_8859_1)
                out.write(byteArrayOf(GS, 0x6B, 0x49, code128Payload.size.toByte()))
                out.write(code128Payload)
            }
            BarcodeFormat.QR_CODE -> {
                // QR Model 2 Function 165, 169, 180, 181
                val clean = code.ifBlank { "POS" }.toByteArray(Charsets.ISO_8859_1)
                // Select Model 2: GS ( k 4 0 49 65 50 0
                out.write(byteArrayOf(GS, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, 0x32, 0x00))
                // Set Size: GS ( k 3 0 49 67 6
                out.write(byteArrayOf(GS, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, 0x06))
                // Store Data: GS ( k pL pH 49 80 48 data
                val len = clean.size + 3
                val pL = (len % 256).toByte()
                val pH = (len / 256).toByte()
                out.write(byteArrayOf(GS, 0x28, 0x6B, pL, pH, 0x31, 0x50, 0x30))
                out.write(clean)
                // Print QR: GS ( k 3 0 49 81 48
                out.write(byteArrayOf(GS, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30))
            }
        }

        // Add line feed
        out.write(0x0A)
        return out.toByteArray()
    }

    /**
     * Generates ESC/POS raster bit image stream (GS v 0) from barcode module boolean array.
     * Ensures universal compatibility across all thermal printers regardless of hardware barcode fonts.
     */
    fun generateBarcodeRasterBytes(
        modules: BooleanArray,
        heightDots: Int = 64,
        scale: Int = 2
    ): ByteArray {
        if (modules.isEmpty()) return ByteArray(0)
        val out = ByteArrayOutputStream()
        val GS: Byte = 0x1D
        val ESC: Byte = 0x1B

        val scaledWidth = modules.size * scale
        val widthBytes = (scaledWidth + 7) / 8

        // Center align
        out.write(byteArrayOf(ESC, 0x61, 0x01))

        // GS v 0 0 xL xH yL yH (Mode 0, normal)
        val xL = (widthBytes % 256).toByte()
        val xH = (widthBytes / 256).toByte()
        val yL = (heightDots % 256).toByte()
        val yH = (heightDots / 256).toByte()
        out.write(byteArrayOf(GS, 0x76, 0x30, 0x00, xL, xH, yL, yH))

        // Build raster bytes for one horizontal row of pixels
        val rowBuffer = ByteArray(widthBytes)
        for (i in modules.indices) {
            if (modules[i]) {
                val startX = i * scale
                for (s in 0 until scale) {
                    val px = startX + s
                    val byteIdx = px / 8
                    val bitIdx = 7 - (px % 8)
                    rowBuffer[byteIdx] = (rowBuffer[byteIdx].toInt() or (1 shl bitIdx)).toByte()
                }
            }
        }

        // Write row repeatedly for heightDots
        repeat(heightDots) {
            out.write(rowBuffer)
        }

        out.write(0x0A)
        return out.toByteArray()
    }

    /**
     * Generates a complete thermal sticker label print job payload (ESC/POS stream).
     */
    fun generateThermalLabelPayload(
        product: Product,
        store: StoreProfile,
        format: BarcodeFormat,
        barcodeString: String,
        is80mm: Boolean = false
    ): ByteArray {
        val out = ByteArrayOutputStream()
        val ESC: Byte = 0x1B
        val GS: Byte = 0x1D
        val LF: Byte = 0x0A

        // Initialize printer
        out.write(byteArrayOf(ESC, 0x40))

        // Center align
        out.write(byteArrayOf(ESC, 0x61, 0x01))

        // Store header (bold)
        out.write(byteArrayOf(ESC, 0x45, 0x01))
        out.write("${store.storeName}\n".toByteArray(Charsets.ISO_8859_1))
        out.write(byteArrayOf(ESC, 0x45, 0x00))

        // Product Name (double height)
        out.write(byteArrayOf(ESC, 0x21, 0x10))
        out.write("${product.name}\n".toByteArray(Charsets.ISO_8859_1))
        out.write(byteArrayOf(ESC, 0x21, 0x00))

        // Price (bold)
        out.write(byteArrayOf(ESC, 0x45, 0x01))
        out.write("${CurrencyFormatter.formatRupiah(product.sellPrice)}\n\n".toByteArray(Charsets.ISO_8859_1))
        out.write(byteArrayOf(ESC, 0x45, 0x00))

        // Barcode
        val barcodeBytes = generateEscPosNativeBarcode(
            code = barcodeString.ifBlank { product.barcode.ifBlank { product.sku } },
            format = format,
            height = if (is80mm) 72 else 54,
            width = if (is80mm) 3 else 2,
            showHri = true
        )
        out.write(barcodeBytes)

        // SKU / Category text
        out.write("SKU: ${product.sku} | ${product.category}\n".toByteArray(Charsets.ISO_8859_1))

        // Feed & Cut
        out.write(byteArrayOf(LF, LF, LF))
        out.write(byteArrayOf(GS, 0x56, 0x42, 0x00))

        return out.toByteArray()
    }
}
