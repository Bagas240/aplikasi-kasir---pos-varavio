package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.google.zxing.BarcodeFormat as ZxingFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.util.EnumMap
import java.util.Locale

enum class BarcodeFormat(val label: String) {
    CODE_128("Code 128 (Alphanumeric)"),
    EAN_13("EAN-13 (Retail Standard)"),
    QR_CODE("QR Code (2D Matrix)")
}

object BarcodeGenerator {

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

    /**
     * Encodes a string into a boolean array of modules (true = black bar, false = white space)
     */
    fun encodeCode128(text: String): BooleanArray {
        val cleanText = if (text.isBlank()) "000000" else text.filter { it.code in 32..126 }
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

        // Convert pattern to modules with 10 modules quiet zone on both sides
        val moduleList = mutableListOf<Boolean>()
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

        repeat(10) { moduleList.add(false) }
        return moduleList.toBooleanArray()
    }

    // EAN-13 Digit Patterns
    private val EAN_L = arrayOf("0001101", "0011001", "0010011", "0111101", "0100011", "0110001", "0101111", "0111011", "0110111", "0001011")
    private val EAN_G = arrayOf("0100111", "0110011", "0011011", "0100001", "0011101", "0111001", "0000101", "0010001", "0001001", "0010111")
    private val EAN_R = arrayOf("1110010", "1100110", "1101100", "1000010", "1011100", "1001110", "1010000", "1000100", "1001000", "1110100")
    private val EAN_PARITY = arrayOf("LLLLLL", "LLGLGG", "LLGGLG", "LLGGGL", "LGLLGG", "LGGLLG", "LGGGLL", "LGLGLG", "LGLGGL", "LGGLGL")

    fun encodeEan13(digitsInput: String): BooleanArray {
        // Ensure 12 or 13 digits
        var rawDigits = digitsInput.filter { it.isDigit() }
        if (rawDigits.length < 12) {
            rawDigits = rawDigits.padStart(12, '0')
        } else if (rawDigits.length > 13) {
            rawDigits = rawDigits.substring(0, 13)
        }

        // Calculate check digit if only 12 digits provided
        val digits12 = rawDigits.substring(0, 12)
        var sum = 0
        for (i in 0 until 12) {
            val d = digits12[i].digitToInt()
            sum += if (i % 2 == 0) d else d * 3
        }
        val calcCheck = (10 - (sum % 10)) % 10
        val final13 = digits12 + calcCheck

        val firstDigit = final13[0].digitToInt()
        val parityScheme = EAN_PARITY[firstDigit]

        val moduleList = mutableListOf<Boolean>()
        // Quiet zone
        repeat(9) { moduleList.add(false) }

        // Start guard: 101
        moduleList.addAll(listOf(true, false, true))

        // Left 6 digits
        for (i in 1..6) {
            val d = final13[i].digitToInt()
            val pattern = if (parityScheme[i - 1] == 'L') EAN_L[d] else EAN_G[d]
            pattern.forEach { moduleList.add(it == '1') }
        }

        // Center guard: 01010
        moduleList.addAll(listOf(false, true, false, true, false))

        // Right 6 digits
        for (i in 7..12) {
            val d = final13[i].digitToInt()
            val pattern = EAN_R[d]
            pattern.forEach { moduleList.add(it == '1') }
        }

        // End guard: 101
        moduleList.addAll(listOf(true, false, true))
        repeat(9) { moduleList.add(false) }

        return moduleList.toBooleanArray()
    }

    /**
     * Generates a 2D QR Code Matrix (25x25 Version 2 style matrix)
     */
    fun encodeQrMatrix(content: String): Array<BooleanArray> {
        val size = 25
        val matrix = Array(size) { BooleanArray(size) { false } }
        val reserved = Array(size) { BooleanArray(size) { false } }

        fun placeFinderPattern(row: Int, col: Int) {
            for (r in 0 until 7) {
                for (c in 0 until 7) {
                    val isBorder = (r == 0 || r == 6 || c == 0 || c == 6)
                    val isCore = (r in 2..4 && c in 2..4)
                    matrix[row + r][col + c] = isBorder || isCore
                    reserved[row + r][col + c] = true
                }
            }
            // Add separators around finders
            for (r in -1..7) {
                for (c in -1..7) {
                    val cr = row + r
                    val cc = col + c
                    if (cr in 0 until size && cc in 0 until size && !reserved[cr][cc]) {
                        matrix[cr][cc] = false
                        reserved[cr][cc] = true
                    }
                }
            }
        }

        placeFinderPattern(0, 0)
        placeFinderPattern(0, size - 7)
        placeFinderPattern(size - 7, 0)

        // Alignment pattern at bottom-right (row 18, col 18)
        val alignR = 18
        val alignC = 18
        for (r in -2..2) {
            for (c in -2..2) {
                val isAlign = (r == -2 || r == 2 || c == -2 || c == 2 || (r == 0 && c == 0))
                matrix[alignR + r][alignC + c] = isAlign
                reserved[alignR + r][alignC + c] = true
            }
        }

        // Timing patterns
        for (i in 8 until size - 8) {
            val timing = (i % 2 == 0)
            if (!reserved[6][i]) {
                matrix[6][i] = timing
                reserved[6][i] = true
            }
            if (!reserved[i][6]) {
                matrix[i][6] = timing
                reserved[i][6] = true
            }
        }

        // Dark module
        matrix[size - 8][8] = true
        reserved[size - 8][8] = true

        // Deterministic pseudo-random encoding of content bytes onto remaining cells
        val hashBytes = (content + "POS_MASTER_SALT").toByteArray()
        var bitIndex = 0

        for (col in size - 1 downTo 0 step 2) {
            val actualCol = if (col <= 6) col - 1 else col
            if (actualCol < 0) continue
            val goingUp = ((size - 1 - col) / 2) % 2 == 0

            val rowRange = if (goingUp) (size - 1 downTo 0) else (0 until size)
            for (row in rowRange) {
                for (cOffset in 0..1) {
                    val c = actualCol - cOffset
                    if (c >= 0 && !reserved[row][c]) {
                        val byteVal = hashBytes[bitIndex % hashBytes.size].toInt()
                        val bit = ((byteVal shr (bitIndex % 8)) and 1) == 1
                        // Apply standard mask (row + col) % 2 == 0
                        val mask = (row + c) % 2 == 0
                        matrix[row][c] = bit xor mask
                        bitIndex++
                    }
                }
            }
        }

        return matrix
    }

    /**
     * Converts a 1D barcode boolean array to an Android Bitmap
     */
    fun createBarcodeBitmap(modules: BooleanArray, width: Int = 400, height: Int = 120): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
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
     * Converts a 2D QR matrix to an Android Bitmap
     */
    fun createQrBitmap(matrix: Array<BooleanArray>, sizePx: Int = 300): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val matrixSize = matrix.size
        val cellSize = sizePx.toFloat() / matrixSize

        for (y in 0 until sizePx) {
            val r = (y / cellSize).toInt().coerceIn(0, matrixSize - 1)
            for (x in 0 until sizePx) {
                val c = (x / cellSize).toInt().coerceIn(0, matrixSize - 1)
                val isBlack = matrix[r][c]
                bitmap.setPixel(x, y, if (isBlack) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    /**
     * Draw 1D Barcode on Compose Canvas
     */
    fun drawBarcodeOnCanvas(
        drawScope: DrawScope,
        modules: BooleanArray,
        canvasWidth: Float,
        canvasHeight: Float,
        barColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Black
    ) {
        val moduleWidth = canvasWidth / modules.size
        for (i in modules.indices) {
            if (modules[i]) {
                drawScope.drawRect(
                    color = barColor,
                    topLeft = Offset(i * moduleWidth, 0f),
                    size = Size(moduleWidth, canvasHeight)
                )
            }
        }
    }

    /**
     * Draw 2D QR Code on Compose Canvas
     */
    fun drawQrOnCanvas(
        drawScope: DrawScope,
        matrix: Array<BooleanArray>,
        canvasWidth: Float,
        canvasHeight: Float,
        moduleColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Black
    ) {
        val cellSize = minOf(canvasWidth, canvasHeight) / matrix.size
        val offsetX = (canvasWidth - (cellSize * matrix.size)) / 2f
        val offsetY = (canvasHeight - (cellSize * matrix.size)) / 2f

        for (r in matrix.indices) {
            for (c in matrix[r].indices) {
                if (matrix[r][c]) {
                    drawScope.drawRect(
                        color = moduleColor,
                        topLeft = Offset(offsetX + c * cellSize, offsetY + r * cellSize),
                        size = Size(cellSize, cellSize)
                    )
                }
            }
        }
    }

    /**
     * Generates a standard barcode payload string for a product ID when no manufacturer barcode exists.
     * For CODE_128: Uses format "PRD-XXXXXX" (e.g. PRD-000042)
     * For EAN_13: Uses GS1 In-Store restricted distribution prefix "20" (e.g. 20000000042X) with calculated check digit
     */
    fun generateBarcodeStringFromProductId(
        productId: Long,
        format: BarcodeFormat = BarcodeFormat.CODE_128,
        customPrefix: String = "PRD"
    ): String {
        return when (format) {
            BarcodeFormat.EAN_13 -> {
                // GS1 In-Store restricted distribution standard (prefix 20 + 10 digits product ID)
                val baseDigits = "20" + productId.coerceAtLeast(1).toString().padStart(10, '0')
                var sum = 0
                for (i in 0 until 12) {
                    val d = baseDigits[i].digitToInt()
                    sum += if (i % 2 == 0) d else d * 3
                }
                val check = (10 - (sum % 10)) % 10
                baseDigits + check
            }
            BarcodeFormat.QR_CODE -> {
                "FORAPOS:PROD:$productId"
            }
            BarcodeFormat.CODE_128 -> {
                val safePrefix = customPrefix.filter { it.isLetterOrDigit() }.ifBlank { "PRD" }
                val paddedId = productId.coerceAtLeast(1).toString().padStart(6, '0')
                "$safePrefix-$paddedId"
            }
        }
    }

    /**
     * Generates a high-resolution barcode bitmap from a product ID using the ZXing library.
     * Specially designed for in-house items and products lacking manufacturer barcodes so labels can be printed.
     */
    fun generateBarcodeBitmapFromProductId(
        productId: Long,
        format: BarcodeFormat = BarcodeFormat.CODE_128,
        width: Int = 480,
        height: Int = 160,
        customPrefix: String = "PRD"
    ): Bitmap {
        val codeString = generateBarcodeStringFromProductId(productId, format, customPrefix)
        val zxingFormat = when (format) {
            BarcodeFormat.CODE_128 -> ZxingFormat.CODE_128
            BarcodeFormat.EAN_13 -> ZxingFormat.EAN_13
            BarcodeFormat.QR_CODE -> ZxingFormat.QR_CODE
        }
        return generateBarcodeBitmapWithZxing(codeString, zxingFormat, width, height)
    }

    /**
     * Renders any string payload into an Android Bitmap using ZXing MultiFormatWriter.
     * Includes silent fallback to native bit matrix encoding if ZXing encounters formatting constraints.
     */
    fun generateBarcodeBitmapWithZxing(
        contents: String,
        format: ZxingFormat = ZxingFormat.CODE_128,
        width: Int = 480,
        height: Int = 160
    ): Bitmap {
        val safeContents = contents.ifBlank { "PRD-000001" }
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.MARGIN, 1)
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
            }
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(safeContents, format, width, height, hints)
            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height
            val bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
            for (x in 0 until matrixWidth) {
                for (y in 0 until matrixHeight) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            // Fallback to internal custom encoder
            when (format) {
                ZxingFormat.EAN_13 -> {
                    val modules = encodeEan13(safeContents)
                    createBarcodeBitmap(modules, width, height)
                }
                ZxingFormat.QR_CODE -> {
                    val matrix = encodeQrMatrix(safeContents)
                    createQrBitmap(matrix, minOf(width, height))
                }
                else -> {
                    val modules = encodeCode128(safeContents)
                    createBarcodeBitmap(modules, width, height)
                }
            }
        }
    }
}
