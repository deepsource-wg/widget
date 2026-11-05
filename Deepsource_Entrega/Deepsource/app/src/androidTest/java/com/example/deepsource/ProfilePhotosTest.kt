package com.example.deepsource

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.deepsource.data.ProfilePhotos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ProfilePhotosTest {
    @Test fun photoSurvivesReloadAndInvalidReplacementDoesNotAffectSavedPhoto() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val directory = context.cacheDir.resolve("profile-capture").apply { mkdirs() }
        val source = File.createTempFile("test-", ".png", directory)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.profile-photos", source)
        val owner = UUID.randomUUID().toString()
        val photos = ProfilePhotos(context)
        try {
            val bitmap = Bitmap.createBitmap(1000, 500, Bitmap.Config.ARGB_8888)
            source.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            val saved = photos.save(owner, uri)
            assertTrue(saved.width in 1..640)
            assertEquals(saved.width, ProfilePhotos(context).load(owner)?.width)
            assertNull(photos.load(UUID.randomUUID().toString()))

            source.writeText("Imagen inválida")
            assertTrue(runCatching { photos.save(owner, uri) }.isFailure)
            assertEquals(saved.width, photos.load(owner)?.width)
            photos.remove(owner)
            assertNull(photos.load(owner))
        } finally {
            photos.remove(owner)
            source.delete()
        }
    }
}
