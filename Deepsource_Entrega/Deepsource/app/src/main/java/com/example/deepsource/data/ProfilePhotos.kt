package com.example.deepsource.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.util.AtomicFile
import java.io.FileNotFoundException
import java.io.IOException
import java.util.UUID

private const val MAX_PHOTO_EDGE = 640

internal class ProfilePhotos(private val context: Context) {
    private val resolver get() = context.contentResolver

    fun load(uid: String): Bitmap? = try {
        file(uid).openRead().use { BitmapFactory.decodeStream(it) }
    } catch (_: FileNotFoundException) {
        null
    }

    fun save(uid: String, source: Uri): Bitmap {
        val bitmap = if (Build.VERSION.SDK_INT >= 28) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, source)) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val width = info.size.width
                val height = info.size.height
                val longest = maxOf(width, height)
                if (longest > MAX_PHOTO_EDGE) {
                    val scale = MAX_PHOTO_EDGE.toFloat() / longest
                    decoder.setTargetSize((width * scale).toInt().coerceAtLeast(1), (height * scale).toInt().coerceAtLeast(1))
                }
            }
        } else {
            decodeLegacyPhoto(source)
        }

        val destination = file(uid)
        val output = destination.startWrite()
        try {
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 88, output)) throw IOException("No se pudo guardar la foto")
            destination.finishWrite(output)
        } catch (error: Exception) {
            destination.failWrite(output)
            throw error
        }
        return bitmap
    }

    fun remove(uid: String) {
        val destination = file(uid)
        destination.delete()
        if (destination.baseFile.exists()) throw IOException("No se pudo quitar la foto")
    }

    private fun file(uid: String): AtomicFile {
        val key = UUID.nameUUIDFromBytes(uid.toByteArray(Charsets.UTF_8))
        return AtomicFile(context.filesDir.resolve("profile-photos/$key.jpg"))
    }

    private fun decodeLegacyPhoto(source: Uri): Bitmap {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, options) }
        val longest = maxOf(options.outWidth, options.outHeight)
        if (longest <= 0) throw IOException("Imagen no disponible")
        options.inSampleSize = 1
        while (longest / options.inSampleSize > MAX_PHOTO_EDGE) options.inSampleSize *= 2
        options.inJustDecodeBounds = false
        val bitmap = resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: throw IOException("Imagen no disponible")
        val orientation = try {
            resolver.openInputStream(source)?.use {
                ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            }
        } catch (_: IOException) {
            null
        }
        val matrix = Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> setScale(-1f, 1f)
                ExifInterface.ORIENTATION_ROTATE_180 -> setRotate(180f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> setScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> { setRotate(90f); postScale(-1f, 1f) }
                ExifInterface.ORIENTATION_ROTATE_90 -> setRotate(90f)
                ExifInterface.ORIENTATION_TRANSVERSE -> { setRotate(270f); postScale(-1f, 1f) }
                ExifInterface.ORIENTATION_ROTATE_270 -> setRotate(270f)
            }
        }
        return if (matrix.isIdentity) bitmap
        else Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
