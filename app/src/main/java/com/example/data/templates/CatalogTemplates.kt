package com.example.data.templates

import android.content.Context
import com.example.data.model.CanvasElement
import com.example.data.model.CanvasElementType
import com.example.data.model.DesignTemplate
import com.example.data.model.ShapeType
import org.json.JSONArray

/**
 * Generated preset catalog (assets/templates/catalog.json). Every design is drawn from our own shapes and
 * text, so there is no third-party artwork. The file is append-only: ids and fingerprints of shipped
 * templates never change. See tools/catalog for the generator.
 */
object CatalogTemplates {
    const val ASSET = "templates/catalog.json"

    fun load(context: Context): List<DesignTemplate> =
        context.assets.open(ASSET).use { parse(it.readBytes().toString(Charsets.UTF_8)) }

    fun parse(json: String): List<DesignTemplate> {
        val arr = JSONArray(json)
        val out = ArrayList<DesignTemplate>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val e = o.getJSONArray("e")
            val els = ArrayList<CanvasElement>(e.length())
            for (j in 0 until e.length()) {
                val a = e.getJSONArray(j)
                if (a.getString(0) == "T") {
                    els += CanvasElement(
                        id = "e$j", type = CanvasElementType.TEXT, text = a.getString(1),
                        x = a.getDouble(2).toFloat(), y = a.getDouble(3).toFloat(), fontSize = a.getDouble(4).toFloat(),
                        isBold = a.getInt(5) == 1, colorHex = a.getString(6), opacity = a.getDouble(7).toFloat()
                    )
                } else {
                    els += CanvasElement(
                        id = "e$j", type = CanvasElementType.SHAPE, shapeType = ShapeType.valueOf(a.getString(1)),
                        x = a.getDouble(2).toFloat(), y = a.getDouble(3).toFloat(),
                        width = a.getDouble(4).toFloat(), height = a.getDouble(5).toFloat(),
                        colorHex = a.getString(6), opacity = a.getDouble(7).toFloat(), rotation = a.getDouble(8).toFloat(),
                        text = a.getString(9).takeIf { it.startsWith("grad") } ?: ""
                    )
                }
            }
            out += DesignTemplate(
                id = o.getString("id"), titleAr = o.getString("tAr"), titleEn = o.getString("tEn"),
                category = o.getString("cat"), aspectRatio = o.getString("asp"),
                isPremium = false, pricingType = "FREE", priceEgp = 0, salesCount = 0,
                elements = els, bgStartHex = o.getString("bs"), bgEndHex = o.getString("be")
            )
        }
        return out
    }
}
