package com.example

import com.example.data.model.Product
import com.example.util.BarcodeItemMatcher
import com.example.util.BarcodeScanResult
import com.google.mlkit.vision.barcode.common.Barcode
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BarcodeScannerHelperTest {

    private lateinit var sampleInventory: List<Product>

    @Before
    fun setUp() {
        sampleInventory = listOf(
            Product(
                id = 1L,
                name = "Kopi Robusta Lampung 250g",
                sku = "BV-KOP-001",
                barcode = "8992753210015",
                category = "Beverages",
                buyPrice = 15000.0,
                sellPrice = 25000.0,
                stock = 50
            ),
            Product(
                id = 2L,
                name = "Matcha Latte 200ml",
                sku = "BV-MAT-002",
                barcode = "8992753210022",
                category = "Beverages",
                buyPrice = 12000.0,
                sellPrice = 22000.0,
                stock = 30
            ),
            Product(
                id = 3L,
                name = "Croissant Butter",
                sku = "BK-CRO-001",
                barcode = "8993456789012",
                category = "Bakery",
                buyPrice = 8000.0,
                sellPrice = 18000.0,
                stock = 15
            ),
            Product(
                id = 4L,
                name = "Imported Chocolate Bar",
                sku = "SN-CHO-999",
                barcode = "012345678905", // 12-digit UPC-A
                category = "Snacks",
                buyPrice = 20000.0,
                sellPrice = 35000.0,
                stock = 40
            )
        )
    }

    @Test
    fun testExactBarcodeMatch() {
        val result = BarcodeItemMatcher.matchProduct("8992753210015", sampleInventory)
        assertNotNull("Product should be found by exact barcode", result)
        assertEquals(1L, result?.id)
        assertEquals("Kopi Robusta Lampung 250g", result?.name)
    }

    @Test
    fun testExactSkuMatch() {
        val result = BarcodeItemMatcher.matchProduct("BV-MAT-002", sampleInventory)
        assertNotNull("Product should be found by exact SKU", result)
        assertEquals(2L, result?.id)
        assertEquals("Matcha Latte 200ml", result?.name)
    }

    @Test
    fun testCaseInsensitiveSkuMatch() {
        val result = BarcodeItemMatcher.matchProduct("bv-mat-002", sampleInventory)
        assertNotNull("Product should be found case-insensitively", result)
        assertEquals(2L, result?.id)
    }

    @Test
    fun testWhitespaceTrimmedMatch() {
        val result = BarcodeItemMatcher.matchProduct("  8993456789012 \n", sampleInventory)
        assertNotNull("Product should be found with trimmed whitespace", result)
        assertEquals(3L, result?.id)
    }

    @Test
    fun testSanitizedAlphanumericMatch() {
        // e.g. barcode entered as BKCRO001 instead of BK-CRO-001
        val result = BarcodeItemMatcher.matchProduct("BKCRO001", sampleInventory)
        assertNotNull("Sanitized alphanumeric SKU match should succeed", result)
        assertEquals(3L, result?.id)
    }

    @Test
    fun testUpcAAndEan13CrossFormatMatch() {
        // Scanned as 13-digit EAN with leading 0: "0012345678905"
        // Target in inventory is 12-digit UPC: "012345678905"
        val result = BarcodeItemMatcher.matchProduct("0012345678905", sampleInventory)
        assertNotNull("EAN-13 with leading zero should map to UPC-A", result)
        assertEquals(4L, result?.id)
    }

    @Test
    fun testNonExistentCodeReturnsNull() {
        val result = BarcodeItemMatcher.matchProduct("9999999999999", sampleInventory)
        assertNull("Non-existent code should return null", result)
    }

    @Test
    fun testSearchProductsByCode() {
        val searchResults = BarcodeItemMatcher.searchProductsByCode("89927532", sampleInventory)
        assertEquals(2, searchResults.size)
        assertTrue(searchResults.any { it.id == 1L })
        assertTrue(searchResults.any { it.id == 2L })
    }

    @Test
    fun testBarcodeFormatNameMapping() {
        assertEquals("EAN-13", BarcodeItemMatcher.getBarcodeFormatName(Barcode.FORMAT_EAN_13))
        assertEquals("Code 128", BarcodeItemMatcher.getBarcodeFormatName(Barcode.FORMAT_CODE_128))
        assertEquals("QR Code", BarcodeItemMatcher.getBarcodeFormatName(Barcode.FORMAT_QR_CODE))
        assertEquals("UPC-A", BarcodeItemMatcher.getBarcodeFormatName(Barcode.FORMAT_UPC_A))
    }

    @Test
    fun testBarcodeScanResultProperties() {
        val matched = sampleInventory[0]
        val scanResult = BarcodeScanResult(
            rawCode = "8992753210015",
            format = Barcode.FORMAT_EAN_13,
            formatName = "EAN-13",
            matchedProduct = matched
        )
        assertTrue(scanResult.isMapped)
        assertEquals("Kopi Robusta Lampung 250g", scanResult.matchedProduct?.name)

        val unmappedResult = BarcodeScanResult(
            rawCode = "999999",
            format = Barcode.FORMAT_CODE_128,
            formatName = "Code 128",
            matchedProduct = null
        )
        assertFalse(unmappedResult.isMapped)
    }
}
