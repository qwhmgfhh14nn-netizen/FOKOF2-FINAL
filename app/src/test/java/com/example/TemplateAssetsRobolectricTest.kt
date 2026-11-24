package com.example

import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.templates.FreeTemplatesData
import com.example.data.templates.ProTemplatesData
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Decodes all 64 bundled WebP artworks the way the app loads them. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TemplateAssetsRobolectricTest {
    @Test fun all_64_artworks_decode() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val all = FreeTemplatesData.getTemplates() + ProTemplatesData.getTemplates()
        val bad = ArrayList<String>()
        for (t in all) {
            val name = t.imageUri?.substringAfterLast('/') ?: run { bad += t.id; null } ?: continue
            try {
                ctx.assets.open("templates/art/$name").use { ins ->
                    val b = BitmapFactory.decodeStream(ins)
                    if (b == null || b.width < 256 || b.height < 256) bad += "${t.id} (decode)"
                }
            } catch (e: Exception) { bad += "${t.id} (${e.javaClass.simpleName})" }
        }
        println("ARTWORK_DECODE_OK=${all.size - bad.size}/${all.size}")
        assertTrue("artworks failing to decode: $bad", bad.isEmpty())
    }
}
