package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.data.cache.ProductDataCache
import com.example.util.cache.InventoryImageCache
import java.io.File

class VoravioApplication : Application(), ImageLoaderFactory {

    val productCache by lazy { ProductDataCache() }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.15) // Limit image memory cache to 15% of heap
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(cacheDir, "pos_images"))
                    .maxSizeBytes(40L * 1024 * 1024) // 40MB
                    .build()
            }
            .allowRgb565(true) // Halves memory consumption on low-end devices
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        InventoryImageCache.onTrimMemory(level)
        productCache.onTrimMemory(level)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        InventoryImageCache.clear()
        productCache.clear()
    }
}
