package com.example.data.cache

import android.content.ComponentCallbacks2
import android.util.LruCache
import com.example.data.model.Product

/**
 * Thread-safe LruCache for caching frequently queried product data, barcode mappings,
 * and search results. Drastically minimizes SQLite disk I/O, GC allocations,
 * and CPU usage on low-end Android devices.
 */
class ProductDataCache(
    maxProducts: Int = 300,
    maxBarcodeLookups: Int = 500
) {
    // Primary cache: Product ID -> Product
    private val idCache = object : LruCache<Long, Product>(maxProducts) {}

    // Barcode/SKU index: Normalized Code -> Product ID for instant O(1) scanner hits
    private val codeToIdCache = object : LruCache<String, Long>(maxBarcodeLookups) {}

    // Search queries cache: Lowercased Query -> List of Product IDs
    private val queryCache = object : LruCache<String, List<Long>>(50) {}

    private val lock = Any()

    fun getById(id: Long): Product? = synchronized(lock) {
        idCache.get(id)
    }

    fun getByBarcodeOrSku(code: String): Product? = synchronized(lock) {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) return null
        val id = codeToIdCache.get(trimmed) ?: return null
        val product = idCache.get(id)
        if (product == null) {
            codeToIdCache.remove(trimmed)
            null
        } else {
            product
        }
    }

    fun put(product: Product) = synchronized(lock) {
        idCache.put(product.id, product)
        if (product.barcode.isNotBlank()) {
            codeToIdCache.put(product.barcode.trim(), product.id)
        }
        if (product.sku.isNotBlank()) {
            codeToIdCache.put(product.sku.trim(), product.id)
        }
        queryCache.evictAll()
    }

    fun putAll(products: Collection<Product>) = synchronized(lock) {
        for (p in products) {
            idCache.put(p.id, p)
            if (p.barcode.isNotBlank()) {
                codeToIdCache.put(p.barcode.trim(), p.id)
            }
            if (p.sku.isNotBlank()) {
                codeToIdCache.put(p.sku.trim(), p.id)
            }
        }
    }

    fun updateStock(productId: Long, newStock: Int) = synchronized(lock) {
        val existing = idCache.get(productId)
        if (existing != null) {
            val updated = existing.copy(stock = newStock)
            idCache.put(productId, updated)
        }
        queryCache.evictAll()
    }

    fun remove(product: Product) = synchronized(lock) {
        removeById(product.id, product.barcode, product.sku)
    }

    fun removeById(id: Long, barcode: String? = null, sku: String? = null) = synchronized(lock) {
        idCache.remove(id)
        if (!barcode.isNullOrBlank()) {
            codeToIdCache.remove(barcode.trim())
        }
        if (!sku.isNullOrBlank()) {
            codeToIdCache.remove(sku.trim())
        }
        queryCache.evictAll()
    }

    fun getCachedQuery(query: String): List<Product>? = synchronized(lock) {
        val ids = queryCache.get(query.trim().lowercase()) ?: return null
        val results = ArrayList<Product>(ids.size)
        for (id in ids) {
            val p = idCache.get(id) ?: return null
            results.add(p)
        }
        results
    }

    fun putCachedQuery(query: String, products: List<Product>) = synchronized(lock) {
        putAll(products)
        val ids = products.map { it.id }
        queryCache.put(query.trim().lowercase(), ids)
    }

    fun clear() = synchronized(lock) {
        idCache.evictAll()
        codeToIdCache.evictAll()
        queryCache.evictAll()
    }

    fun onTrimMemory(level: Int) = synchronized(lock) {
        when {
            level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                clear()
            }
            level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW ||
            level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                idCache.trimToSize(idCache.maxSize() / 2)
                codeToIdCache.trimToSize(codeToIdCache.maxSize() / 2)
                queryCache.evictAll()
            }
        }
    }

    val productCount: Int get() = synchronized(lock) { idCache.size() }
    val barcodeCount: Int get() = synchronized(lock) { codeToIdCache.size() }
}
