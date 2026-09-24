package com.example.util.cache

import android.content.ComponentCallbacks2
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import java.io.File

/**
 * High-performance LruCache for inventory images and product bitmaps, specifically tuned
 * for low-end Android devices.
 *
 * Features:
 * - Dynamic cache sizing based on total available JVM heap (default: 1/8th of max heap in KB).
 * - Bitmap memory tracking by exact byte count (via `bitmap.byteCount / 1024`).
 * - Downsampled image decoding with `inPreferredConfig = Bitmap.Config.RGB_565` (50% RAM savings vs ARGB_8888).
 * - Automatic memory trimming on OS memory warnings (`onTrimMemory` / `onLowMemory`).
 */
object InventoryImageCache {
    private const val TAG = "InventoryImageCache"

    // Default max heap fraction: 1/8th of available runtime memory
    private val maxMemoryKb: Int = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSizeKb: Int = (maxMemoryKb / 8).coerceIn(4096, 32768) // Min 4MB, Max 32MB

    private val lruCache = object : LruCache<String, Bitmap>(cacheSizeKb) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            val bytes = bitmap.byteCount
            return if (bytes > 0) bytes / 1024 else 1
        }
    }

    val currentSizeKb: Int
        get() = synchronized(lruCache) { lruCache.size() }

    val maxSizeKb: Int
        get() = synchronized(lruCache) { lruCache.maxSize() }

    fun get(key: String): Bitmap? {
        if (key.isBlank()) return null
        return synchronized(lruCache) {
            val bmp = lruCache.get(key)
            if (bmp != null && bmp.isRecycled) {
                lruCache.remove(key)
                null
            } else {
                bmp
            }
        }
    }

    fun put(key: String, bitmap: Bitmap) {
        if (key.isBlank() || bitmap.isRecycled) return
        synchronized(lruCache) {
            lruCache.put(key, bitmap)
        }
    }

    fun remove(key: String): Bitmap? {
        if (key.isBlank()) return null
        return synchronized(lruCache) {
            lruCache.remove(key)
        }
    }

    fun clear() {
        synchronized(lruCache) {
            lruCache.evictAll()
        }
    }

    /**
     * Decode a local image file with target dimensions and RGB_565 config,
     * retrieving from cache if already loaded.
     */
    fun getOrDecode(
        filePath: String,
        reqWidth: Int = 256,
        reqHeight: Int = 256
    ): Bitmap? {
        if (filePath.isBlank()) return null
        val cacheKey = "${filePath}_${reqWidth}x${reqHeight}"

        get(cacheKey)?.let { return it }

        val file = File(filePath)
        if (!file.exists() || !file.canRead() || file.length() == 0L) return null

        try {
            // 1. Measure dimensions without allocating full pixel memory
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, options)

            if (options.outWidth <= 0 || options.outHeight <= 0) return null

            // 2. Compute sample size to avoid allocating full image
            options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            // RGB_565 uses 2 bytes per pixel instead of 4 bytes for ARGB_8888, saving 50% memory
            options.inPreferredConfig = Bitmap.Config.RGB_565

            val decoded = BitmapFactory.decodeFile(file.absolutePath, options) ?: return null
            put(cacheKey, decoded)
            return decoded
        } catch (oom: OutOfMemoryError) {
            Log.e(TAG, "OOM decoding image: $filePath, clearing cache", oom)
            clear()
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding image: $filePath", e)
            return null
        }
    }

    private fun calculateInSampleSize(rawWidth: Int, rawHeight: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (rawHeight > reqHeight || rawWidth > reqWidth) {
            val halfHeight = rawHeight / 2
            val halfWidth = rawWidth / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    fun onTrimMemory(level: Int) {
        synchronized(lruCache) {
            when {
                level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL ||
                level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                    lruCache.evictAll()
                }
                level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW ||
                level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                    lruCache.trimToSize(cacheSizeKb / 2)
                }
                level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> {
                    lruCache.trimToSize(cacheSizeKb / 3)
                }
            }
        }
    }
}
