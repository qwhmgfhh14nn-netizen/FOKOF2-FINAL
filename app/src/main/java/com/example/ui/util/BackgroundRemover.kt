package com.example.ui.util

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions

/** Cuts the main subject out of a photo with Google ML Kit (free). The model downloads once on first use and needs Google Play services. */
object BackgroundRemover {
    fun remove(src: Bitmap, onDone: (Bitmap?, String?) -> Unit) {
        val seg = SubjectSegmentation.getClient(SubjectSegmenterOptions.Builder().enableForegroundBitmap().build())
        seg.process(InputImage.fromBitmap(src, 0))
            .addOnSuccessListener { r -> onDone(r.foregroundBitmap, null); seg.close() }
            .addOnFailureListener { e -> onDone(null, e.message ?: "failed"); seg.close() }
    }
}
