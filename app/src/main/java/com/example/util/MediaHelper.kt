package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max

object MediaHelper {

    private const val MAX_IMAGE_DIMENSION = 1024

    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, prefix: String = "img"): String? {
        return try {
            val dir = File(context.filesDir, "pos_media").apply { if (!exists()) mkdirs() }
            val file = File(dir, "${prefix}_${System.currentTimeMillis()}.jpg")
            val scaledBitmap = scaleBitmapDownIfNeeded(bitmap, MAX_IMAGE_DIMENSION)
            FileOutputStream(file).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            if (scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun copyUriToInternalStorage(context: Context, uri: Uri, prefix: String = "img"): String? {
        return try {
            val dir = File(context.filesDir, "pos_media").apply { if (!exists()) mkdirs() }
            val file = File(dir, "${prefix}_${System.currentTimeMillis()}.jpg")

            // Decode dimensions first to calculate sample size (low memory footprint)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }

            val sampleSize = calculateInSampleSize(options.outWidth, options.outHeight, MAX_IMAGE_DIMENSION, MAX_IMAGE_DIMENSION)
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565 // 50% less memory than ARGB_8888 on low-end devices
            }

            val decodedBitmap = context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            }

            if (decodedBitmap != null) {
                FileOutputStream(file).use { output ->
                    decodedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
                }
                decodedBitmap.recycle()
            } else {
                // Fallback: direct stream copy if bitmap decoding was not applicable
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                inputStream?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun scaleBitmapDownIfNeeded(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val maxSide = max(width, height)
        if (maxSide <= maxDimension) return bitmap

        val ratio = maxDimension.toFloat() / maxSide
        val targetWidth = (width * ratio).toInt().coerceAtLeast(1)
        val targetHeight = (height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    fun loadCachedBitmap(filePath: String, reqWidth: Int = 256, reqHeight: Int = 256): Bitmap? {
        return com.example.util.cache.InventoryImageCache.getOrDecode(filePath, reqWidth, reqHeight)
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
}
