package com.example.ui.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

/** Real export of a rendered design: PNG to Pictures/FOKOF2, PDF to Downloads/FOKOF2 (Android 10+), and the share sheet. */
object DesignExporter {
    fun savePng(ctx: Context, bmp: Bitmap, name: String): Uri? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        return try {
            val v = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$name.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/FOKOF2")
            }
            val uri = ctx.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v) ?: return null
            ctx.contentResolver.openOutputStream(uri)?.use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) } ?: return null
            uri
        } catch (e: Exception) { null }
    }

    /** One page per bitmap; the page has the bitmap's own pixel size so nothing is cropped. */
    fun savePdf(ctx: Context, pages: List<Bitmap>, name: String): Uri? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || pages.isEmpty()) return null
        return try {
            val doc = PdfDocument()
            pages.forEachIndexed { i, b ->
                val page = doc.startPage(PdfDocument.PageInfo.Builder(b.width, b.height, i + 1).create())
                page.canvas.drawBitmap(b, 0f, 0f, null)
                doc.finishPage(page)
            }
            val v = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, "$name.pdf")
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, "Download/FOKOF2")
            }
            val uri = ctx.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v)
            if (uri == null) { doc.close(); return null }
            ctx.contentResolver.openOutputStream(uri)?.use { doc.writeTo(it) }
            doc.close()
            uri
        } catch (e: Exception) { null }
    }

    fun share(ctx: Context, uri: Uri, mime: String) {
        val i = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        ctx.startActivity(Intent.createChooser(i, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
