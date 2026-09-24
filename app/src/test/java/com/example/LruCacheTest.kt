package com.example

import android.content.ComponentCallbacks2
import android.graphics.Bitmap
import com.example.data.cache.ProductDataCache
import com.example.data.model.Product
import com.example.util.cache.InventoryImageCache
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LruCacheTest {

    private lateinit var productCache: ProductDataCache

    @Before
    fun setUp() {
        productCache = ProductDataCache(maxProducts = 5, maxBarcodeLookups = 5)
        InventoryImageCache.clear()
    }

    private fun createProduct(id: Long, name: String, barcode: String, sku: String, stock: Int = 10): Product {
        return Product(
            id = id,
            name = name,
            sku = sku,
            barcode = barcode,
            category = "General",
            buyPrice = 5000.0,
            sellPrice = 10000.0,
            stock = stock
        )
    }

    @Test
    fun testPutAndGetById() {
        val p1 = createProduct(1L, "Kopi Susu", "899111", "SKU-01")
        productCache.put(p1)

        val retrieved = productCache.getById(1L)
        assertNotNull(retrieved)
        assertEquals("Kopi Susu", retrieved?.name)
        assertEquals("SKU-01", retrieved?.sku)
    }

    @Test
    fun testLookupByBarcodeAndSku() {
        val p = createProduct(10L, "Teh Botol", "899222333", "SKU-TB-10")
        productCache.put(p)

        // Lookup by barcode
        val byBarcode = productCache.getByBarcodeOrSku("899222333")
        assertNotNull(byBarcode)
        assertEquals(10L, byBarcode?.id)
        assertEquals("Teh Botol", byBarcode?.name)

        // Lookup by SKU
        val bySku = productCache.getByBarcodeOrSku("SKU-TB-10")
        assertNotNull(bySku)
        assertEquals(10L, bySku?.id)

        // Non-existent code returns null
        assertNull(productCache.getByBarcodeOrSku("UNKNOWN-999"))
    }

    @Test
    fun testUpdateStockKeepsCacheSynchronized() {
        val p = createProduct(20L, "Roti Coklat", "899444", "SKU-RC", stock = 15)
        productCache.put(p)

        productCache.updateStock(20L, 8)

        val updated = productCache.getById(20L)
        assertNotNull(updated)
        assertEquals(8, updated?.stock)
    }

    @Test
    fun testRemoveInvalidatesBothIdAndBarcodeCaches() {
        val p = createProduct(30L, "Susu UHT", "899555", "SKU-UHT")
        productCache.put(p)

        assertNotNull(productCache.getById(30L))
        assertNotNull(productCache.getByBarcodeOrSku("899555"))

        productCache.remove(p)

        assertNull(productCache.getById(30L))
        assertNull(productCache.getByBarcodeOrSku("899555"))
        assertNull(productCache.getByBarcodeOrSku("SKU-UHT"))
    }

    @Test
    fun testLruEvictionWhenCapacityExceeded() {
        // Cache has maxProducts = 5
        for (i in 1..5) {
            productCache.put(createProduct(i.toLong(), "Item $i", "BAR-$i", "SKU-$i"))
        }
        assertEquals(5, productCache.productCount)

        // Access Item 1 so it becomes most recently used
        productCache.getById(1L)

        // Insert 6th item -> item 2 (least recently used) should be evicted
        productCache.put(createProduct(6L, "Item 6", "BAR-6", "SKU-6"))

        assertNotNull(productCache.getById(1L))
        assertNotNull(productCache.getById(6L))
        assertNull(productCache.getById(2L))
    }

    @Test
    fun testTrimMemoryUnderPressure() {
        for (i in 1..5) {
            productCache.put(createProduct(i.toLong(), "Item $i", "BAR-$i", "SKU-$i"))
        }
        assertEquals(5, productCache.productCount)

        // Simulate moderate memory pressure -> trims 50%
        productCache.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW)
        assertTrue(productCache.productCount <= 3)

        // Simulate critical memory pressure -> clear all
        productCache.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL)
        assertEquals(0, productCache.productCount)
    }

    @Test
    fun testInventoryImageCacheOperations() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.RGB_565)
        val key = "sample_img_key"

        InventoryImageCache.put(key, bitmap)
        val cached = InventoryImageCache.get(key)
        assertNotNull(cached)
        assertEquals(100, cached?.width)
        assertEquals(100, cached?.height)

        // Test remove
        InventoryImageCache.remove(key)
        assertNull(InventoryImageCache.get(key))

        // Test clear
        InventoryImageCache.put(key, bitmap)
        InventoryImageCache.clear()
        assertNull(InventoryImageCache.get(key))
    }
}
