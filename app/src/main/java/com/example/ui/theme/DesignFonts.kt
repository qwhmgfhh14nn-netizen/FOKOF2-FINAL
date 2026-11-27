package com.example.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.R

/** Arabic font pack (all SIL Open Font License, bundled; no network use at runtime). Key "" = Cairo. */
object DesignFonts {
    data class Entry(val key: String, val label: String, val family: FontFamily)

    private fun fam(reg: Int, bold: Int? = null) = FontFamily(
        Font(reg, FontWeight.Normal),
        Font(bold ?: reg, FontWeight.Bold)
    )

    val all: List<Entry> = listOf(
        Entry("", "كايرو", CairoFontFamily),
        Entry("tajawal", "تجوال", fam(R.font.tajawal_regular, R.font.tajawal_bold)),
        Entry("almarai", "المراعي", fam(R.font.almarai_regular, R.font.almarai_bold)),
        Entry("amiri", "أميري", fam(R.font.amiri_regular, R.font.amiri_bold)),
        Entry("lalezar", "لاليزار", fam(R.font.lalezar_regular)),
        Entry("aref", "عارف رقعة", fam(R.font.aref_regular, R.font.aref_bold)),
        Entry("harmattan", "هرمتان", fam(R.font.harmattan_regular, R.font.harmattan_bold)),
        Entry("rakkas", "رقاص", fam(R.font.rakkas_regular)),
        Entry("katibeh", "كتيبة", fam(R.font.katibeh_regular))
    )

    fun family(key: String): FontFamily = all.firstOrNull { it.key == key }?.family ?: CairoFontFamily
}
