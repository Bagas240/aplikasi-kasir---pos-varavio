package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.Product
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Voravio", appName)
    }

    @Test
    fun `test product room schema and barcode retrieval`() = runBlocking {
        val testProduct = Product(
            name = "Kopi Susu Espresso",
            sku = "KOP-001",
            barcode = "8992753998877",
            category = "Minuman",
            buyPrice = 10000.0,
            sellPrice = 18000.0,
            stock = 75,
            minStockAlert = 10,
            unit = "Cup",
            description = "Espresso robusta dengan susu kental manis"
        )

        val insertedId = db.posDao().insertProduct(testProduct)
        val retrieved = db.posDao().getProductByBarcodeOrSku("8992753998877")

        assertNotNull(retrieved)
        assertEquals("Kopi Susu Espresso", retrieved?.name)
        assertEquals(18000.0, retrieved?.sellPrice ?: 0.0, 0.001)
        assertEquals(10000.0, retrieved?.buyPrice ?: 0.0, 0.001)
        assertEquals(75, retrieved?.stock)
        assertEquals("8992753998877", retrieved?.barcode)
        assertEquals("KOP-001", retrieved?.sku)

        // Test stock update
        db.posDao().updateStock(retrieved!!.id, 70)
        val updated = db.posDao().getProductById(retrieved.id)
        assertEquals(70, updated?.stock)
    }
}

