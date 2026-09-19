package com.example

import com.example.data.model.Product
import com.example.ui.PosViewModel
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for captured product codes and core calculation logic.
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testCapturedProductCodeDataModel() {
        val testProduct = Product(
            id = 1L,
            name = "Kopi Susu Espresso",
            sku = "KOP-001",
            barcode = "8992753123456",
            category = "Minuman",
            buyPrice = 10000.0,
            sellPrice = 18000.0,
            stock = 50,
            minStockAlert = 5,
            unit = "Cup",
            description = "Espresso with milk"
        )

        val captured = PosViewModel.CapturedProductCode(
            code = "8992753123456",
            product = testProduct
        )

        assertEquals("8992753123456", captured.code)
        assertNotNull(captured.product)
        assertEquals("Kopi Susu Espresso", captured.product?.name)
        assertEquals(18000.0, captured.product?.sellPrice ?: 0.0, 0.001)
    }

    @Test
    fun testCapturedProductCodeDeduplication() {
        val list = mutableListOf<PosViewModel.CapturedProductCode>()
        val code1 = PosViewModel.CapturedProductCode(code = "8991001", product = null)
        val code2 = PosViewModel.CapturedProductCode(code = "8991002", product = null)
        val code3 = PosViewModel.CapturedProductCode(code = "8991001", product = null)

        // Adding code1 then code2
        val state1 = listOf(code1)
        val state2 = (listOf(code2) + state1.filterNot { it.code == code2.code }).take(10)
        assertEquals(2, state2.size)
        assertEquals("8991002", state2[0].code)

        // Adding code3 (duplicate of code1) moves it to head without duplicating count
        val state3 = (listOf(code3) + state2.filterNot { it.code == code3.code }).take(10)
        assertEquals(2, state3.size)
        assertEquals("8991001", state3[0].code)
        assertEquals("8991002", state3[1].code)
    }
}

