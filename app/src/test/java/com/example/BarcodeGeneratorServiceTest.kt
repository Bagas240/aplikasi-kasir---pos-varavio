package com.example

import com.example.data.model.Product
import com.example.data.model.StoreProfile
import com.example.util.BarcodeFormat
import com.example.util.BarcodeGeneratorService
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BarcodeGeneratorServiceTest {

    private lateinit var service: BarcodeGeneratorService

    @Before
    fun setUp() {
        service = BarcodeGeneratorService()
    }

    @Test
    fun testEan13CheckDigitCalculation() {
        // Known retail test cases:
        // Case 1: "899275321001" -> Check digit should be 5
        val check1 = service.calculateEan13CheckDigit("899275321001")
        assertEquals(5, check1)

        // Full code should be "8992753210015"
        val full1 = service.generateEan13FromDigits("899275321001")
        assertEquals("8992753210015", full1)
        assertTrue(service.validateEan13(full1))

        // Case 2: "400638133393" -> 1
        val check2 = service.calculateEan13CheckDigit("400638133393")
        assertEquals(1, check2)
        assertTrue(service.validateEan13("4006381333931"))
    }

    @Test
    fun testEan13GenerationAndValidation() {
        val generated = service.generateEan13("899")
        assertEquals(13, generated.length)
        assertTrue("Generated EAN-13 must start with 899", generated.startsWith("899"))
        assertTrue("Generated EAN-13 must pass validation", service.validateEan13(generated))

        // Corrupting the check digit must fail validation
        val lastChar = generated.last()
        val corruptLast = if (lastChar == '0') '1' else '0'
        val corrupted = generated.dropLast(1) + corruptLast
        assertFalse(service.validateEan13(corrupted))
    }

    @Test
    fun testEan13EncodingStructure() {
        val code = "8992753210015"
        val modules = service.encodeEan13(code)
        // EAN-13 standard module count: 9 (quiet) + 3 (start 101) + 42 (left 6 digits) + 5 (center 01010) + 42 (right 6 digits) + 3 (end 101) + 9 (quiet) = 113
        assertEquals(113, modules.size)

        // Verify start guard 101 at offset 9
        assertTrue(modules[9])
        assertFalse(modules[10])
        assertTrue(modules[11])

        // Verify center guard 01010 at offset 9 + 3 + 42 = 54
        assertFalse(modules[54])
        assertTrue(modules[55])
        assertFalse(modules[56])
        assertTrue(modules[57])
        assertFalse(modules[58])

        // Verify end guard 101 at offset 54 + 5 + 42 = 101
        assertTrue(modules[101])
        assertFalse(modules[102])
        assertTrue(modules[103])

        // Test bit string
        val bitString = service.encodeEan13BitString(code)
        assertEquals(113, bitString.length)
        assertEquals('1', bitString[9])
        assertEquals('0', bitString[10])
        assertEquals('1', bitString[11])
    }

    @Test
    fun testEan13HumanReadableFormatting() {
        val formatted = service.formatEan13HumanReadable("8992753210015")
        assertEquals("8 992753 210015", formatted)
        assertEquals("8 992753 210015", service.formatHumanReadable("8992753210015", BarcodeFormat.EAN_13))
    }

    @Test
    fun testCode128GenerationAndValidation() {
        val generated = service.generateCode128(prefix = "SKU")
        assertTrue(generated.startsWith("SKU-"))
        assertTrue(service.validateCode128(generated))

        val custom = service.generateCode128("COFFEE-BEANS-01")
        assertEquals("COFFEE-BEANS-01", custom)
        assertTrue(service.validateCode128(custom))

        // Non-ASCII or control characters
        assertFalse(service.validateCode128(""))
        assertFalse(service.validateCode128("Line\nBreak"))
    }

    @Test
    fun testCode128CheckDigitAndEncoding() {
        val text = "POS-1001"
        val checkDigit = service.calculateCode128CheckDigit(text)
        assertTrue("Check digit must be in 0..102", checkDigit in 0..102)

        val modules = service.encodeCode128(text)
        assertTrue("Modules should not be empty", modules.isNotEmpty())

        // Code 128 formula: 10 + 11 (start) + 11 * 8 (chars) + 11 (check) + 13 (stop) + 10 = 142
        val expectedLength = 10 + 11 + (11 * text.length) + 11 + 13 + 10
        assertEquals(expectedLength, modules.size)

        val bitString = service.encodeCode128BitString(text)
        assertEquals(expectedLength, bitString.length)
        assertTrue(bitString.startsWith("0000000000")) // 10 quiet modules
        assertTrue(bitString.endsWith("0000000000"))   // 10 quiet modules
    }

    @Test
    fun testBarcodeBitmapCreation() {
        val modules = service.encodeEan13("8992753210015")
        val bitmap = service.createBarcodeBitmap(modules, width = 226, height = 80)
        assertNotNull(bitmap)
        assertEquals(226, bitmap.width)
        assertEquals(80, bitmap.height)
    }

    @Test
    fun testEscPosNativeBarcodeGeneration() {
        val ean13Bytes = service.generateEscPosNativeBarcode(
            code = "8992753210015",
            format = BarcodeFormat.EAN_13,
            height = 60,
            width = 2,
            showHri = true
        )
        assertTrue("ESC/POS EAN-13 bytes should not be empty", ean13Bytes.isNotEmpty())

        val code128Bytes = service.generateEscPosNativeBarcode(
            code = "POS-ITEM-001",
            format = BarcodeFormat.CODE_128,
            height = 50,
            width = 2,
            showHri = true
        )
        assertTrue("ESC/POS Code 128 bytes should not be empty", code128Bytes.isNotEmpty())
    }

    @Test
    fun testBarcodeRasterStreamGeneration() {
        val modules = service.encodeCode128("TEST123")
        val rasterBytes = service.generateBarcodeRasterBytes(modules, heightDots = 32, scale = 2)
        assertTrue("Raster stream should not be empty", rasterBytes.isNotEmpty())
    }

    @Test
    fun testThermalLabelPayloadGeneration() {
        val dummyProduct = Product(
            name = "Premium Robusta 250g",
            sku = "COF-001",
            barcode = "8991234567890",
            category = "Beverages",
            buyPrice = 25000.0,
            sellPrice = 45000.0,
            stock = 100
        )
        val dummyStore = StoreProfile(
            storeName = "Kopi Nusantara",
            address = "Jl. Merdeka No. 10",
            phone = "08123456789"
        )
        val labelBytes = service.generateThermalLabelPayload(
            product = dummyProduct,
            store = dummyStore,
            format = BarcodeFormat.EAN_13,
            barcodeString = dummyProduct.barcode,
            is80mm = false
        )
        assertTrue("Thermal label payload must not be empty", labelBytes.isNotEmpty())
    }
}
