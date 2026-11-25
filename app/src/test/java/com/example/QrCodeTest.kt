package com.example

import com.example.ui.util.QrCodeMaker
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Encodes then decodes the QR to prove it scans back to the link. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class QrCodeTest {
    @Test fun qr_roundtrip() {
        val link = "https://wa.me/201206449934"
        val bmp = QrCodeMaker.make(link, 0xFF1E293B.toInt(), 400)
        val px = IntArray(400 * 400); bmp.getPixels(px, 0, 400, 0, 0, 400, 400)
        val out = QRCodeReader().decode(BinaryBitmap(HybridBinarizer(RGBLuminanceSource(400, 400, px)))).text
        println("QR_ROUNDTRIP=$out")
        assertEquals(link, out)
    }
}
