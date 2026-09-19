package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object MediaHelper {

    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, prefix: String = "img"): String? {
        return try {
            val dir = File(context.filesDir, "pos_media").apply { if (!exists()) mkdirs() }
            val file = File(dir, "${prefix}_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
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
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
