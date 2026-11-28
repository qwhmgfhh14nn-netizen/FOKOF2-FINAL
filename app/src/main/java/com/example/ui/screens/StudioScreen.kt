package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatShapes
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.itemsIndexed
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CanvasElement
import com.example.data.model.CanvasElementType
import com.example.data.model.DesignTemplate
import com.example.data.model.ShapeType
import com.example.ui.locale.TranslationStrings
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.util.parseColor
import com.example.ui.components.DESIGN_W
import com.example.ui.components.DesignBackground
import com.example.ui.components.DesignLayer
import com.example.ui.components.aspectRatioValue
import kotlin.math.roundToInt

@Composable
fun StudioScreen(
    strings: TranslationStrings,
    canvasElements: List<CanvasElement>,
    selectedElementId: String?,
    bgStartHex: String,
    bgEndHex: String,
    canvasBgImageRes: Int? = null,
    canvasBgImageUri: String? = null,
    templates: List<DesignTemplate>,
    canvasAspectRatio: String = "1:1",
    onSelectElement: (String?) -> Unit,
    onAddText: (String) -> Unit,
    onAddShape: (ShapeType) -> Unit,
    onAddCalligraphy: (String) -> Unit,
    onAddGraphicSticker: (String) -> Unit,
    onSetCanvasBgPhoto: (Int?, String?) -> Unit = { _, _ -> },
    onUpdateElement: ((CanvasElement) -> CanvasElement) -> Unit,
    onUpdateElementById: (String, (CanvasElement) -> CanvasElement) -> Unit = { _, f -> onUpdateElement(f) },
    onDeleteElement: () -> Unit,
    onDuplicateElement: () -> Unit,
    onClearCanvas: () -> Unit,
    onAddImage: (String) -> Unit = {},
    pageCount: Int = 1,
    onMoveLayer: (Int) -> Unit = {},
    pageIndex: Int = 0,
    onSelectPage: (Int) -> Unit = {},
    onAddPage: (Boolean) -> Unit = {},
    onDeletePage: () -> Unit = {},
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
    onSetCanvasAspectRatio: (String) -> Unit,
    onSetCanvasBg: (String, String) -> Unit,
    onLoadTemplate: (DesignTemplate) -> Unit,
    onSaveProject: () -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddTextDialog by remember { mutableStateOf(false) }
    var newTextInput by remember { mutableStateOf("") }
    var showTemplatesDialog by remember { mutableStateOf(false) }
    var showCalligraphyDialog by remember { mutableStateOf(false) }
    var showImagePickerDialog by remember { mutableStateOf(false) }
    var showReplacePhotoDialog by remember { mutableStateOf(false) }
    val galleryContext = androidx.compose.ui.platform.LocalContext.current
    val galleryPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { picked ->
        if (picked != null) {
            val saved = runCatching {
                val dir = java.io.File(galleryContext.filesDir, "design_bg").apply { mkdirs() }
                val out = java.io.File(dir, "bg_" + System.currentTimeMillis() + ".jpg")
                galleryContext.contentResolver.openInputStream(picked)?.use { input ->
                    out.outputStream().use { input.copyTo(it) }
                }
                if (out.length() > 0L) android.net.Uri.fromFile(out).toString() else null
            }.getOrNull()
            if (saved != null) {
                onSetCanvasBgPhoto(null, saved)
                onShowToast("تم تعيين صورتك من المعرض كخلفية للتصميم")
            } else {
                onShowToast("ما قدرناش نفتح الصورة دي، جرب صورة تانية")
            }
            showReplacePhotoDialog = false
        }
    }
    val addPhotoPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { picked ->
        if (picked != null) {
            val saved = runCatching {
                val dir = java.io.File(galleryContext.filesDir, "design_img").apply { mkdirs() }
                val out = java.io.File(dir, "img_" + System.currentTimeMillis() + ".jpg")
                galleryContext.contentResolver.openInputStream(picked)?.use { input -> out.outputStream().use { input.copyTo(it) } }
                if (out.length() > 0L) android.net.Uri.fromFile(out).toString() else null
            }.getOrNull()
            if (saved != null) onAddImage(saved) else onShowToast("ما قدرناش نفتح الصورة دي، جرب صورة تانية")
        }
    }
    var showResizeDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    val lockedIds = remember { androidx.compose.runtime.mutableStateListOf<String>() }
    var capturing by remember { mutableStateOf(false) }
    val exportLayer = androidx.compose.ui.graphics.rememberGraphicsLayer()
    val exportScope = androidx.compose.runtime.rememberCoroutineScope()
    var showBgColorDialog by remember { mutableStateOf(false) }

    val selectedElement = canvasElements.find { it.id == selectedElementId }

    // Color Swatches
    val colorPresets = listOf(
        "#D4AF37", // Signature Gold
        "#F3E5AB", // Champagne Gold
        "#E5C158", // Bright Gold
        "#FFFFFF", // Pure White
        "#121216", // Obsidian Black
        "#10B981", // Emerald Green
        "#EF4444", // Ruby Red
        "#3B82F6", // Sapphire Blue
        "#8B5CF6", // Royal Purple
        "#C0C0C0"  // Platinum Silver
    )

    val backgroundPresets = listOf(
        Pair("#0C0C10", "#1D1912"), // Obsidian & Gold
        Pair("#14141A", "#0B0B0E"), // Pure Luxury Dark
        Pair("#251D0F", "#100D08"), // Royal Bronze
        Pair("#FFFFFF", "#F4F0E6"), // Pearl White
        Pair("#1E1012", "#0F0B0C"), // Deep Velvet Ruby
        Pair("#0A192F", "#020C1B")  // Midnight Navy
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Title & Studio Top Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = strings.studioTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        fontFamily = CairoFontFamily
                    )
                )
                Text(
                    text = "${strings.studioSubtitle} • [${canvasAspectRatio}]",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Row {
                IconButton(onClick = onUndo, enabled = canUndo) {
                    Text("↶", color = if (canUndo) GoldPrimary else GoldPrimary.copy(alpha = 0.3f), style = MaterialTheme.typography.titleLarge)
                }
                IconButton(onClick = onRedo, enabled = canRedo) {
                    Text("↷", color = if (canRedo) GoldPrimary else GoldPrimary.copy(alpha = 0.3f), style = MaterialTheme.typography.titleLarge)
                }
                IconButton(onClick = { drawMode = !drawMode; onSelectElement(null) }) {
                    Icon(imageVector = Icons.Default.Brush, contentDescription = "رسم حر", tint = if (drawMode) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { showResizeDialog = true }) {
                    Icon(imageVector = Icons.Default.AspectRatio, contentDescription = "Resize", tint = GoldPrimary)
                }
                IconButton(onClick = { showBgColorDialog = true }) {
                    Icon(imageVector = Icons.Default.FormatColorFill, contentDescription = "Background", tint = GoldPrimary)
                }
                IconButton(onClick = onSaveProject) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "Save Project", tint = GoldPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            items(pageCount) { i ->
                FilterChip(selected = i == pageIndex, onClick = { onSelectPage(i) }, label = { Text("صفحة ${i + 1}") })
            }
            item { AssistChip(onClick = { onAddPage(false) }, label = { Text("+ صفحة جديدة") }) }
            item { AssistChip(onClick = { onAddPage(true) }, label = { Text("نسخ الصفحة") }) }
            if (pageCount > 1) item { AssistChip(onClick = onDeletePage, label = { Text("حذف الصفحة") }) }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Quick Tools Toolbar
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                StudioToolChip(
                    icon = Icons.Default.AddPhotoAlternate,
                    label = "أضف صورة",
                    onClick = { addPhotoPicker.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                )
            }
            item {
                StudioToolChip(
                    icon = Icons.Default.AddPhotoAlternate,
                    label = "تغيير الصورة",
                    onClick = { showReplacePhotoDialog = true }
                )
            }
            item {
                StudioToolChip(
                    icon = Icons.Default.TextFields,
                    label = strings.addText,
                    onClick = {
                        newTextInput = ""
                        showAddTextDialog = true
                    }
                )
            }
            item {
                StudioToolChip(
                    icon = Icons.Default.Brush,
                    label = strings.addCalligraphy,
                    onClick = { showCalligraphyDialog = true }
                )
            }
            item {
                StudioToolChip(
                    icon = Icons.Default.FormatShapes,
                    label = strings.addShape,
                    onClick = { onAddShape(ShapeType.ROUNDED_RECT) }
                )
            }
            item {
                StudioToolChip(
                    icon = Icons.Default.BookmarkBorder,
                    label = strings.templates,
                    onClick = { showTemplatesDialog = true }
                )
            }
            item {
                StudioToolChip(
                    icon = Icons.Default.CleaningServices,
                    label = strings.clearCanvas,
                    onClick = onClearCanvas
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (drawMode) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("#D4AF37", "#FFFFFF", "#1A1A1A", "#E53935", "#1E88E5", "#43A047").forEach { c ->
                    Box(
                        Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(parseColor(c, GoldPrimary))
                            .border(2.dp, if (drawColor == c) GoldPrimary else Color.Transparent, CircleShape)
                            .clickable { drawColor = c }
                    )
                }
                Slider(
                    value = drawStroke,
                    onValueChange = { drawStroke = it },
                    valueRange = 2f..18f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = GoldPrimary, activeTrackColor = GoldPrimary)
                )
            }
            Text("ارسم بصباعك على التصميم. كل خط بيتحفظ كعنصر تقدر تحركه أو تحجمه أو تمسحه.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // THE INTERACTIVE DESIGN CANVAS (same element renderer + design space as gallery/preview)
        val canvasRatio = aspectRatioValue(canvasAspectRatio)

        BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val maxCanvasH = 560.dp
            val fullH = maxWidth / canvasRatio
            val canvasH = if (fullH > maxCanvasH) maxCanvasH else fullH
            val canvasW = canvasH * canvasRatio
            val k = canvasW.value / DESIGN_W
            Card(
                modifier = Modifier
                    .size(canvasW, canvasH)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, GoldBorder, RoundedCornerShape(16.dp))
                    .testTag("interactive_design_canvas"),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().clickable { onSelectElement(null) }.then(
                    if (capturing) Modifier.drawWithContent {
                        exportLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(exportLayer)
                    } else Modifier
                )) {
                    DesignBackground(
                        bgStartHex, bgEndHex, canvasBgImageUri, canvasBgImageRes,
                        Modifier.fillMaxSize()
                    )
                    DesignLayer(
                        elements = canvasElements,
                        k = k,
                        modifier = Modifier.fillMaxSize(),
                        selectedId = if (drawMode) null else selectedElementId,
                        onSelect = if (drawMode) null else onSelectElement,
                        onMove = if (drawMode) null else ({ id, dx, dy ->
                            if (id !in lockedIds) onUpdateElementById(id) { cur ->
                                cur.copy(
                                    x = (cur.x + dx).coerceIn(0f, DESIGN_W),
                                    y = (cur.y + dy).coerceIn(0f, DESIGN_W / canvasRatio)
                                )
                            }
                        })
                    )
                    if (drawMode) {
                        val densityNow = LocalDensity.current.density
                        val pxPerUnit = k * densityNow
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(drawColor, drawStroke) {
                                    detectDragGestures(
                                        onDragStart = { o -> drawPts.clear(); drawPts.add(o) },
                                        onDragEnd = {
                                            if (drawPts.size > 1) {
                                                val pts = drawPts.map { Offset(it.x / pxPerUnit, it.y / pxPerUnit) }
                                                val minX = pts.minOf { it.x }; val maxX = pts.maxOf { it.x }
                                                val minY = pts.minOf { it.y }; val maxY = pts.maxOf { it.y }
                                                val w = (maxX - minX).coerceAtLeast(8f)
                                                val h = (maxY - minY).coerceAtLeast(8f)
                                                val rel = pts.joinToString(";") { "${(it.x - minX).roundToInt()},${(it.y - minY).roundToInt()}" }
                                                onAddDraw(
                                                    CanvasElement(
                                                        type = CanvasElementType.DRAW,
                                                        x = minX + w / 2f,
                                                        y = minY + h / 2f,
                                                        width = w,
                                                        height = h,
                                                        colorHex = drawColor,
                                                        strokeW = drawStroke,
                                                        points = rel
                                                    )
                                                )
                                            }
                                            drawPts.clear()
                                        }
                                    ) { change, _ ->
                                        change.consume()
                                        drawPts.add(change.position)
                                    }
                                }
                        ) {
                            if (drawPts.size > 1) {
                                val live = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(drawPts[0].x, drawPts[0].y)
                                    for (i in 1 until drawPts.size) lineTo(drawPts[i].x, drawPts[i].y)
                                }
                                drawPath(
                                    live,
                                    parseColor(drawColor, GoldPrimary),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = drawStroke * pxPerUnit,
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                                    )
                                )
                            }
                        }
                    }
                    if (canvasElements.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = strings.selectElementPrompt,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Element Inspector / Properties Panel
        if (selectedElement != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("element_inspector_panel"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${strings.layers}: ${selectedElement.type.name}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                        )

                        Row {
                            IconButton(
                                onClick = { if (selectedElement.id in lockedIds) lockedIds.remove(selectedElement.id) else lockedIds.add(selectedElement.id) },
                                modifier = Modifier.size(32.dp)
                            ) { Text(if (selectedElement.id in lockedIds) "🔒" else "🔓") }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = onDuplicateElement,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = onDeleteElement,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item { AssistChip(onClick = { onMoveLayer(2) }, label = { Text("لقدّام خالص") }) }
                        item { AssistChip(onClick = { onMoveLayer(1) }, label = { Text("طبقة لقدّام") }) }
                        item { AssistChip(onClick = { onMoveLayer(-1) }, label = { Text("طبقة لورا") }) }
                        item { AssistChip(onClick = { onMoveLayer(-2) }, label = { Text("لورا خالص") }) }
                    }

                    // Text Editing
                    if (selectedElement.type == CanvasElementType.TEXT || selectedElement.type == CanvasElementType.CALLIGRAPHY) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = selectedElement.text,
                            onValueChange = { newTxt ->
                                onUpdateElement { it.copy(text = newTxt) }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("محتوى النص") },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.FormatSize, contentDescription = "Font Size", tint = GoldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Slider(
                                value = selectedElement.fontSize,
                                onValueChange = { size ->
                                    onUpdateElement { it.copy(fontSize = size) }
                                },
                                valueRange = 12f..64f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = GoldPrimary,
                                    activeTrackColor = GoldPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${selectedElement.fontSize.toInt()}sp",
                                style = MaterialTheme.typography.bodySmall,
                                color = GoldPrimary
                            )

                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { onUpdateElement { it.copy(isBold = !it.isBold) } },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatBold,
                                    contentDescription = "Bold",
                                    tint = if (selectedElement.isBold) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            listOf(0 to "وسط", 1 to "يمين", 2 to "شمال").forEach { (a, label) ->
                                val on = selectedElement.align == a
                                Text(
                                    text = label,
                                    color = if (on) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .border(1.dp, if (on) GoldPrimary else MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                        .clickable {
                                            onUpdateElement { it.copy(align = a, width = if (a != 0 && it.width < 200f) 280f else it.width) }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("تأثير النص", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(listOf(0 to "بدون", 1 to "ظل", 2 to "حد خارجي", 3 to "ظل + حد")) { (fx, label) ->
                                val on = selectedElement.textFx == fx
                                Text(
                                    text = label,
                                    color = if (on) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .border(1.dp, if (on) GoldPrimary else MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                        .clickable { onUpdateElement { it.copy(textFx = fx) } }
                                        .padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(com.example.ui.theme.DesignFonts.all) { fnt ->
                                val on = selectedElement.font == fnt.key
                                Text(
                                    text = fnt.label,
                                    fontFamily = fnt.family,
                                    color = if (on) GoldPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .border(1.dp, if (on) GoldPrimary else MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                        .clickable { onUpdateElement { it.copy(font = fnt.key) } }
                                        .padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    if (selectedElement.type == CanvasElementType.IMAGE) {
                        Text("شكل القص", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            itemsIndexed(com.example.ui.components.imageShapeNames) { i, nm ->
                                val on = selectedElement.imgShape == i
                                Text(nm, color = if (on) GoldPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.border(1.dp, if (on) GoldPrimary else MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                        .clickable { onUpdateElement { it.copy(imgShape = i) } }.padding(horizontal = 12.dp, vertical = 5.dp))
                            }
                        }
                        Text("الفلتر", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            itemsIndexed(com.example.ui.components.imageFilterNames) { i, nm ->
                                val on = selectedElement.filter == i
                                Text(nm, color = if (on) GoldPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.border(1.dp, if (on) GoldPrimary else MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                        .clickable { onUpdateElement { it.copy(filter = i) } }.padding(horizontal = 12.dp, vertical = 5.dp))
                            }
                        }
                        Text("الحجم", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            listOf("صغير" to 100f, "وسط" to 160f, "كبير" to 240f, "عريض" to -1f).forEach { (nm, w) ->
                                Text(nm, color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                        .clickable { onUpdateElement { it.copy(width = if (w < 0f) 320f else w, height = if (w < 0f) 180f else w) } }.padding(horizontal = 12.dp, vertical = 5.dp))
                            }
                        }
                    }

                    // Shape size & opacity slider
                    if (selectedElement.type == CanvasElementType.SHAPE) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "الحجم والتناغم", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = selectedElement.scale,
                            onValueChange = { s -> onUpdateElement { it.copy(scale = s) } },
                            valueRange = 0.5f..2.5f,
                            colors = SliderDefaults.colors(thumbColor = GoldPrimary, activeTrackColor = GoldPrimary)
                        )
                    }

                    // Color Palette
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = strings.color, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(colorPresets) { hex ->
                            val color = parseColor(hex, Color.White)
                            val isColorSelected = selectedElement.colorHex.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isColorSelected) 2.5.dp else 1.dp,
                                        color = if (isColorSelected) GoldPrimary else Color.Gray.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                                    .clickable { onUpdateElement { it.copy(colorHex = hex) } }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save & Export Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onSaveProject,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("studio_save_project_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = Color(0xFF101014)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = strings.saveProject, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { onSelectElement(null); showExportDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("studio_export_design_button"),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary)
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = strings.exportDesign, color = GoldPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Export: renders the canvas exactly as it looks on screen (at this phone's screen resolution) to a real file.
    if (showExportDialog) {
        val doExport: (String) -> Unit = { mode ->
            showExportDialog = false
            exportScope.launch {
                capturing = true
                kotlinx.coroutines.delay(350)
                try {
                    val bmp = exportLayer.toImageBitmap().asAndroidBitmap().copy(android.graphics.Bitmap.Config.ARGB_8888, false)
                    val name = "FOKOF2_" + System.currentTimeMillis()
                    if (mode == "pdfall") {
                        val startPage = pageIndex
                        val bmps = ArrayList<android.graphics.Bitmap>()
                        for (pi in 0 until pageCount) {
                            onSelectPage(pi)
                            kotlinx.coroutines.delay(500)
                            bmps.add(exportLayer.toImageBitmap().asAndroidBitmap().copy(android.graphics.Bitmap.Config.ARGB_8888, false))
                        }
                        onSelectPage(startPage)
                        val u = com.example.ui.util.DesignExporter.savePdf(galleryContext, bmps, name)
                        onShowToast(if (u != null) "تم حفظ PDF بـ ${bmps.size} صفحة في Download/FOKOF2" else "تعذر حفظ الملف (يحتاج أندرويد 10 أو أحدث)")
                    } else if (mode == "pdf") {
                        val u = com.example.ui.util.DesignExporter.savePdf(galleryContext, listOf(bmp), name)
                        onShowToast(if (u != null) "تم حفظ ملف PDF في مجلد Download/FOKOF2" else "تعذر حفظ الملف (يحتاج أندرويد 10 أو أحدث)")
                    } else {
                        val u = com.example.ui.util.DesignExporter.savePng(galleryContext, bmp, name)
                        if (u == null) onShowToast("تعذر حفظ الصورة (يحتاج أندرويد 10 أو أحدث)")
                        else if (mode == "share") com.example.ui.util.DesignExporter.share(galleryContext, u, "image/png")
                        else onShowToast("تم حفظ الصورة في معرض الصور (مجلد FOKOF2)")
                    }
                } catch (e: Exception) {
                    onShowToast("حصلت مشكلة أثناء التصدير، جرب تاني")
                } finally {
                    capturing = false
                }
            }
        }
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text(text = strings.exportDesign, color = GoldPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("الملف بيطلع بنفس دقة شاشة موبايلك.", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { doExport("png") }, modifier = Modifier.fillMaxWidth()) { Text("حفظ صورة PNG في المعرض") }
                    Button(onClick = { doExport("pdf") }, modifier = Modifier.fillMaxWidth()) { Text(if (pageCount > 1) "حفظ PDF (الصفحة الحالية)" else "حفظ PDF") }
                    if (pageCount > 1) Button(onClick = { doExport("pdfall") }, modifier = Modifier.fillMaxWidth()) { Text("حفظ PDF لكل الصفحات ($pageCount)") }
                    OutlinedButton(onClick = { doExport("share") }, modifier = Modifier.fillMaxWidth()) { Text("مشاركة الصورة") }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showExportDialog = false }) { Text("إلغاء", color = GoldPrimary) } }
        )
    }

    // Dialog 1: Add Text
    if (showAddTextDialog) {
        AlertDialog(
            onDismissRequest = { showAddTextDialog = false },
            title = { Text(text = strings.addText, color = GoldPrimary) },
            text = {
                OutlinedTextField(
                    value = newTextInput,
                    onValueChange = { newTextInput = it },
                    placeholder = { Text("اكتب نص التصميم...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTextInput.isNotBlank()) onAddText(newTextInput) else onAddText(strings.sampleText)
                        showAddTextDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("إضافة", color = Color(0xFF101014), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTextDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Dialog: Replace Photographic Image (Canva-style)
    if (showReplacePhotoDialog) {
        AlertDialog(
            onDismissRequest = { showReplacePhotoDialog = false },
            title = {
                Text(
                    text = "استبدال صورة التصميم (Canva Style)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CairoFontFamily
                    )
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "اختر صورة فوتوغرافية احترافية واقعية لتطبيقها كخلفية للتصميم:",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    // From phone gallery
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.5.dp, GoldPrimary, RoundedCornerShape(12.dp))
                            .clickable {
                                galleryPicker.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Gallery",
                                tint = GoldPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "من المعرض (صورك أنت)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = GoldPrimary)
                                )
                                Text(
                                    text = "اختار أي صورة من موبايلك واستخدمها كخلفية للتصميم",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                    }

                    // Photo 1: Royal Charcoal Burger
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, GoldBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                onSetCanvasBgPhoto(R.drawable.img_restaurant_food, null)
                                onShowToast("تم تعيين صورة برجر الفحم الملكي المشوي!")
                                showReplacePhotoDialog = false
                            },
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_restaurant_food),
                                contentDescription = "Burger",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "🍔 برجر الفحم الملكي المشوي",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GoldPrimary
                                    )
                                )
                                Text(
                                    text = "صورة فوتوغرافية واقعية - إعلانات مطاعم وجبات سريعة",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }

                    // Photo 2: Italian Wood-Fired Pizza
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, GoldBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                onSetCanvasBgPhoto(R.drawable.img_restaurant_pizza, null)
                                onShowToast("تم تعيين صورة بيتزا الحطب الإيطالية!")
                                showReplacePhotoDialog = false
                            },
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_restaurant_pizza),
                                contentDescription = "Pizza",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "🍕 بيتزا الحطب الإيطالية الأصيلة",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GoldPrimary
                                    )
                                )
                                Text(
                                    text = "صورة فوتوغرافية واقعية - إعلانات بيتزا ومطاعم إيطالية",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }

                    // Photo 3: Brand Kit / Luxury Dark
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, GoldBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                onSetCanvasBgPhoto(R.drawable.template_brand_kit, null)
                                onShowToast("تم تعيين خلفية الهوية البصرية الملكية!")
                                showReplacePhotoDialog = false
                            },
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.template_brand_kit),
                                contentDescription = "Brand Kit",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "👑 هوية تجارية ملكية فاخرة",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GoldPrimary
                                    )
                                )
                                Text(
                                    text = "خلفية أعمال وهوية بصرية فخمة",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }

                    // Remove Photo Option (Gradient Background)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .clickable {
                                onSetCanvasBgPhoto(null, null)
                                onShowToast("تمت إزالة الصورة والعودة للخلفية المتدرجة")
                                showReplacePhotoDialog = false
                            },
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = "Clear Photo",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "إزالة الصورة والعودة للألوان والتدرجات",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showReplacePhotoDialog = false }) {
                    Text("إلغاء", color = GoldPrimary)
                }
            }
        )
    }

    // Dialog 2: Image / Graphics Picker
    if (showImagePickerDialog) {
        val imageOptions = listOf(
            "🦅 شعار الصقر الملكي" to "🦅",
            "👑 التاج الذهبي" to "👑",
            "✨ بريق الإبداع والنجوم" to "✨",
            "⚡ صاعقة الطاقة والتسويق" to "⚡",
            "💎 جوهرة الفخامة" to "💎",
            "☕ رمز القهوة المختصة" to "☕",
            "🎯 رمز دقة التصويب" to "🎯",
            "🌟 نجمة التميز الذهبية" to "🌟"
        )
        AlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            title = { Text(text = strings.uploadImage, color = GoldPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "اختر رسماً أو ملصقاً فكتورياً أو أضف شعارًا مخصصًا:", style = MaterialTheme.typography.bodySmall)
                    imageOptions.forEach { (label, symbol) ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    onAddGraphicSticker(symbol)
                                    showImagePickerDialog = false
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = symbol, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = label, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showImagePickerDialog = false }) { Text("إغلاق") } }
        )
    }

    // Dialog 3: Resize Design (Aspect Ratio)
    if (showResizeDialog) {
        val ratios = listOf(
            "1:1" to "مربع (انستغرام وبوستات)",
            "9:16" to "عمودي (ستوري وريلز وتيك توك)",
            "16:9" to "أفقي (يوتيوب وبانرات المواقع)",
            "4:5" to "بورتريه احترافي (إعلانات فيسبوك وبوست انستجرام عمودي)",
            "3:4" to "ورقة A4 / سيرة ذاتية / كتاب",
            "4:3" to "شهادة / عرض عادي / غلاف فيسبوك"
        )
        AlertDialog(
            onDismissRequest = { showResizeDialog = false },
            title = { Text(text = strings.resizeDesign, color = GoldPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ratios.forEach { (ratio, desc) ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (canvasAspectRatio == ratio) 1.5.dp else 1.dp,
                                    color = if (canvasAspectRatio == ratio) GoldPrimary else GoldBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    onSetCanvasAspectRatio(ratio)
                                    showResizeDialog = false
                                },
                            color = if (canvasAspectRatio == ratio) GoldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "$ratio - $desc", fontWeight = FontWeight.Bold, color = if (canvasAspectRatio == ratio) GoldPrimary else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showResizeDialog = false }) { Text("إغلاق") } }
        )
    }

    // Dialog 4: Background Colors
    if (showBgColorDialog) {
        AlertDialog(
            onDismissRequest = { showBgColorDialog = false },
            title = { Text(text = "تغيير لون وخلفية التصميم", color = GoldPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    backgroundPresets.forEachIndexed { idx, (startHex, endHex) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(parseColor(startHex, Color.Black), parseColor(endHex, Color.DarkGray))
                                    )
                                )
                                .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    onSetCanvasBg(startHex, endHex)
                                    showBgColorDialog = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "نمط خلفية فخمة #$idx",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showBgColorDialog = false }) { Text("إغلاق") } }
        )
    }

    // Dialog 5: Calligraphy
    if (showCalligraphyDialog) {
        val calligraphyPresets = listOf(
            "الابتكار والإبداع",
            "أفكارك تتحول إلى تصاميم قبل أن تتكلم",
            "FOKOF2 للإبداع والتصميم",
            "فخامة وأصالة مصرية",
            "عرض حصري وخاص",
            "FOKOF2 CREATIVE"
        )
        AlertDialog(
            onDismissRequest = { showCalligraphyDialog = false },
            title = { Text(text = strings.addCalligraphy, color = GoldPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    calligraphyPresets.forEach { preset ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    onAddCalligraphy(preset)
                                    showCalligraphyDialog = false
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = preset,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary,
                                    fontFamily = CairoFontFamily
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showCalligraphyDialog = false }) { Text("إغلاق") } }
        )
    }

    // Dialog 6: Load Templates
    if (showTemplatesDialog) {
        AlertDialog(
            onDismissRequest = { showTemplatesDialog = false },
            title = { Text(text = strings.templates, color = GoldPrimary) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    templates.forEach { tpl ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLoadTemplate(tpl)
                                    showTemplatesDialog = false
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = tpl.titleAr,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = GoldPrimary
                                        )
                                    )
                                    Text(
                                        text = "${tpl.category} • ${tpl.aspectRatio} • ${if (tpl.priceEgp > 0) "${tpl.priceEgp} ${strings.currencyEgp}" else strings.freeBadge}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showTemplatesDialog = false }) { Text("إغلاق") } }
        )
    }
}

@Composable
fun StudioToolChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, GoldBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = GoldPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
fun RenderElement(element: CanvasElement) {
    val elementColor = parseColor(element.colorHex, GoldPrimary)

    when (element.type) {
        CanvasElementType.TEXT, CanvasElementType.CALLIGRAPHY -> {
            Text(
                text = element.text,
                fontSize = element.fontSize.sp,
                fontWeight = if (element.isBold) FontWeight.Bold else FontWeight.Normal,
                color = elementColor,
                fontFamily = CairoFontFamily
            )
        }
        CanvasElementType.SHAPE -> {
            val shapeModifier = Modifier
                .size(element.width.dp, element.height.dp)
                .background(elementColor.copy(alpha = element.opacity))

            when (element.shapeType) {
                ShapeType.CIRCLE -> Box(modifier = shapeModifier.clip(CircleShape))
                ShapeType.ROUNDED_RECT -> Box(modifier = shapeModifier.clip(RoundedCornerShape(12.dp)))
                ShapeType.RECTANGLE -> Box(modifier = shapeModifier)
                ShapeType.DIAMOND -> Box(modifier = shapeModifier.rotate(45f).clip(RoundedCornerShape(4.dp)))
                ShapeType.STAR -> Box(
                    modifier = Modifier
                        .size(element.width.dp, element.height.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "★",
                        fontSize = element.fontSize.sp,
                        color = elementColor
                    )
                }
                ShapeType.HEXAGON -> Box(modifier = shapeModifier.clip(RoundedCornerShape(8.dp)))
            }
        }
        CanvasElementType.IMAGE -> {}
        CanvasElementType.DRAW -> {}
        CanvasElementType.IMAGE_BADGE -> {
            Box(
                modifier = Modifier
                    .size(element.width.dp, element.height.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, elementColor, RoundedCornerShape(8.dp))
                    .background(elementColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = element.text.ifBlank { "BARAYA" },
                    color = elementColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

