package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.CanvasElement
import com.example.data.model.CanvasElementType
import com.example.data.model.DesignTemplate
import com.example.data.model.ShapeType
import com.example.ui.theme.CairoFontFamily
import com.example.ui.util.parseColor
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/** Shared design space: every template/studio design is laid out in 360 units of width. */
const val DESIGN_W = 360f

fun aspectRatioValue(s: String?): Float = when (s) {
    "16:9" -> 16f / 9f
    "9:16" -> 9f / 16f
    "4:5" -> 4f / 5f
    "3:4" -> 3f / 4f
    "4:3" -> 4f / 3f
    else -> 1f
}

/** One element, drawn at design scale [k] (dp per design unit). Same renderer for gallery, preview and studio. */
@Composable
fun DesignElementContent(el: CanvasElement, k: Float) {
    val density = LocalDensity.current
    val color = parseColor(el.colorHex, Color(0xFFD4AF37))
    when (el.type) {
        CanvasElementType.TEXT, CanvasElementType.CALLIGRAPHY -> {
            val fs = with(density) { (el.fontSize * k).dp.toSp() }
            val light = (color.red * 0.299f + color.green * 0.587f + color.blue * 0.114f) > 0.75f
            Text(
                text = el.text,
                fontSize = fs,
                lineHeight = fs * 1.3f,
                fontWeight = if (el.isBold) FontWeight.Bold else FontWeight.Normal,
                fontFamily = CairoFontFamily,
                color = color.copy(alpha = el.opacity),
                textAlign = TextAlign.Center,
                style = if (light) TextStyle(shadow = Shadow(Color.Black.copy(alpha = 0.35f), Offset(0f, 2f), 4f)) else TextStyle.Default,
                modifier = Modifier.widthIn(max = (330f * k).dp)
            )
        }
        CanvasElementType.SHAPE -> {
            Canvas(modifier = Modifier.size((el.width * k).dp, (el.height * k).dp)) {
                val c = color.copy(alpha = el.opacity)
                val w = size.width
                val h = size.height
                when (el.shapeType) {
                    ShapeType.RECTANGLE -> drawRect(c)
                    ShapeType.ROUNDED_RECT -> drawRoundRect(c, cornerRadius = CornerRadius(min(w, h) * 0.18f))
                    ShapeType.CIRCLE -> drawOval(c)
                    ShapeType.DIAMOND -> drawPath(Path().apply {
                        moveTo(w / 2f, 0f); lineTo(w, h / 2f); lineTo(w / 2f, h); lineTo(0f, h / 2f); close()
                    }, c)
                    ShapeType.HEXAGON -> drawPath(Path().apply {
                        moveTo(w * 0.25f, 0f); lineTo(w * 0.75f, 0f); lineTo(w, h / 2f)
                        lineTo(w * 0.75f, h); lineTo(w * 0.25f, h); lineTo(0f, h / 2f); close()
                    }, c)
                    ShapeType.STAR -> drawPath(Path().apply {
                        val cx = w / 2f; val cy = h / 2f
                        val ro = min(w, h) / 2f; val ri = ro * 0.42f
                        for (i in 0 until 10) {
                            val r = if (i % 2 == 0) ro else ri
                            val a = Math.PI / 5.0 * i - Math.PI / 2.0
                            val px = cx + (r * cos(a)).toFloat(); val py = cy + (r * sin(a)).toFloat()
                            if (i == 0) moveTo(px, py) else lineTo(px, py)
                        }
                        close()
                    }, c)
                }
            }
        }
        CanvasElementType.IMAGE_BADGE -> {
            Box(
                modifier = Modifier
                    .size((el.width * k).dp, (el.height * k).dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, color, RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = el.text.ifBlank { "FOKOF2" }, color = color, fontSize = (12f * k).sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Lays every element out by its design-space centre (x, y). When [onSelect]/[onMove] are given the layer is
 * interactive (studio); otherwise it is a static render (gallery, preview).
 */
@Composable
fun DesignLayer(
    elements: List<CanvasElement>,
    k: Float,
    modifier: Modifier = Modifier,
    selectedId: String? = null,
    onSelect: ((String?) -> Unit)? = null,
    onMove: ((String, Float, Float) -> Unit)? = null
) {
    val density = LocalDensity.current.density
    val pxPerUnit = k * density
    val currentOnMove by rememberUpdatedState(onMove)
    val currentOnSelect by rememberUpdatedState(onSelect)
    Layout(
        modifier = modifier,
        content = {
            elements.forEach { el ->
                key(el.id) {
                    var m: Modifier = Modifier
                    if (onSelect != null) {
                        m = m
                            .pointerInput(el.id) { detectTapGestures { currentOnSelect?.invoke(el.id) } }
                            .pointerInput(el.id, pxPerUnit) {
                                detectDragGestures { change, drag ->
                                    change.consume()
                                    currentOnSelect?.invoke(el.id)
                                    currentOnMove?.invoke(el.id, drag.x / pxPerUnit, drag.y / pxPerUnit)
                                }
                            }
                    }
                    m = m.graphicsLayer {
                        rotationZ = el.rotation
                        scaleX = el.scale
                        scaleY = el.scale
                    }
                    if (el.id == selectedId) {
                        m = m.border(1.5.dp, Color(0xFFD4AF37), RoundedCornerShape(4.dp)).padding(3.dp)
                    }
                    Box(modifier = m) { DesignElementContent(el, k) }
                }
            }
        }
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(Constraints()) }
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { i, p ->
                val el = elements[i]
                p.place(
                    (el.x * pxPerUnit - p.width / 2f).roundToInt(),
                    (el.y * pxPerUnit - p.height / 2f).roundToInt()
                )
            }
        }
    }
}

/** Gradient + photo background with explicit failure feedback (no silent solid fill). */
@Composable
fun DesignBackground(
    bgStartHex: String,
    bgEndHex: String,
    imageUri: String?,
    imageRes: Int?,
    modifier: Modifier = Modifier
) {
    var failed by remember(imageUri) { mutableStateOf(false) }
    Box(
        modifier = modifier.background(
            Brush.linearGradient(listOf(parseColor(bgStartHex, Color(0xFFF8F1E4)), parseColor(bgEndHex, Color(0xFFE9D8A6))))
        )
    ) {
        if (imageRes != null) {
            Image(painter = painterResource(id = imageRes), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else if (!imageUri.isNullOrBlank()) {
            val ctx = LocalContext.current
            AsyncImage(
                model = ImageRequest.Builder(ctx)
                    .data(imageUri)
                    .size(coil.size.Size(1024, 1024)) // bounded, shared decode for gallery/preview/studio
                    .memoryCacheKey(imageUri)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onError = { failed = true },
                onSuccess = { failed = false }
            )
            if (failed) {
                Text(
                    text = "⚠ تعذر تحميل صورة القالب",
                    color = Color(0xFFB91C1C),
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp)
                )
            }
        }
    }
}

/** Static, element-driven rendering of a whole template (art + every element at its own position/size). */
@Composable
fun TemplateDesignView(template: DesignTemplate, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.clip(RoundedCornerShape(8.dp))) {
        val k = maxWidth.value / DESIGN_W
        DesignBackground(
            template.bgStartHex, template.bgEndHex, template.imageUri, template.previewDrawableRes,
            Modifier.fillMaxSize()
        )
        DesignLayer(template.elements, k, Modifier.fillMaxSize())
    }
}

/**
 * Gallery/preview thumbnail that respects the template's own aspect ratio (9:16 stays tall, 16:9 wide).
 * Overlays (badges, buttons) are drawn on top of the thumbnail area.
 */
@Composable
fun TemplateThumb(
    template: DesignTemplate,
    maxHeight: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {}
) {
    val ratio = aspectRatioValue(template.aspectRatio)
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val hFull = maxWidth / ratio
        val h = if (hFull > maxHeight) maxHeight else hFull
        val w = h * ratio
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(h)
                .background(Color(0xFFF3EAD3)),
            contentAlignment = Alignment.Center
        ) {
            TemplateVisualCanvas(template = template, modifier = Modifier.size(w, h))
            overlay()
        }
    }
}
