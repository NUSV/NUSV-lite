package com.nusv.lite.ui.screens.tools

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gradient
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke as ComposeStroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.nusv.lite.util.LocalAppStrings
import com.nusv.lite.util.performIfEnabled
import java.lang.System
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class BrushShape { ROUND, SQUARE }

enum class BrushMode { NORMAL, SPRAY, MARKER, RAINBOW, CALLIGRAPHY }

data class Stroke(
    val color: Color,
    val width: Float,
    val points: MutableList<Offset>,
    val alpha: Float = 1f,
    val shape: BrushShape = BrushShape.ROUND,
    val mode: BrushMode = BrushMode.NORMAL,
    val timestamps: MutableList<Long> = mutableListOf(),
    val startHue: Float = 0f,
)

private val presetColors = listOf(
    Color.Black, Color(0xFF424242), Color(0xFF795548),
    Color.Red, Color(0xFFE91E63), Color(0xFF9C27B0),
    Color.Blue, Color(0xFF2196F3), Color(0xFF03A9F4),
    Color(0xFF009688), Color.Green, Color(0xFF8BC34A),
    Color(0xFFCDDC39), Color.Yellow, Color(0xFFFFC107),
    Color(0xFFFF9800), Color(0xFFFF5722), Color(0xFF795548),
)

private val brushSizeOptions = listOf(3f, 8f, 16f, 30f, 50f)

/** Renders the strokes onto a bitmap and saves it to the gallery. Returns false when saving fails. */
fun saveDrawing(context: Context, strokes: List<Stroke>, width: Int, height: Int): Boolean {
    return try {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        for (stroke in strokes) {
            if (stroke.points.size < 2) continue
            val isEraser = stroke.mode == BrushMode.NORMAL && stroke.color == Color.White
            val paint = android.graphics.Paint().apply {
                color = if (isEraser) android.graphics.Color.WHITE else android.graphics.Color.argb(
                    (stroke.alpha * 255).toInt(),
                    (stroke.color.red * 255).toInt(),
                    (stroke.color.green * 255).toInt(),
                    (stroke.color.blue * 255).toInt()
                )
                strokeWidth = stroke.width
                style = android.graphics.Paint.Style.STROKE
                strokeCap = if (stroke.shape == BrushShape.ROUND) android.graphics.Paint.Cap.ROUND else android.graphics.Paint.Cap.SQUARE
                strokeJoin = android.graphics.Paint.Join.ROUND
                isAntiAlias = true
            }
            val path = android.graphics.Path()
            path.moveTo(stroke.points[0].x, stroke.points[0].y)
            for (i in 1 until stroke.points.size) {
                path.lineTo(stroke.points[i].x, stroke.points[i].y)
            }
            canvas.drawPath(path, paint)
        }
        val filename = "Drawing_${System.currentTimeMillis()}.png"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/NUSV")
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: return false
        val output = context.contentResolver.openOutputStream(uri) ?: return false
        output.use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        true
    } catch (_: Exception) {
        false
    }
}

fun colorsToGradientStops(vararg colors: Pair<Float, Color>): List<Pair<Float, Color>> = colors.toList()

fun DrawScope.drawStroke(stroke: Stroke, density: Float) {
    when (stroke.mode) {
        BrushMode.SPRAY -> drawSprayStroke(stroke)
        BrushMode.RAINBOW -> drawRainbowStroke(stroke)
        BrushMode.CALLIGRAPHY -> drawCalligraphyStroke(stroke, density)
        else -> drawNormalStroke(stroke)
    }
}

private fun DrawScope.drawNormalStroke(stroke: Stroke) {
    if (stroke.points.size < 2) return
    val cap = if (stroke.shape == BrushShape.ROUND) StrokeCap.Round else StrokeCap.Square
    val color = stroke.color.copy(alpha = stroke.color.alpha * stroke.alpha)
    val path = Path().apply {
        moveTo(stroke.points[0].x, stroke.points[0].y)
        for (i in 1 until stroke.points.size) {
            lineTo(stroke.points[i].x, stroke.points[i].y)
        }
    }
    drawPath(path, color, style = ComposeStroke(width = stroke.width, cap = cap, join = StrokeJoin.Round))
}

private fun DrawScope.drawSprayStroke(stroke: Stroke) {
    if (stroke.points.size < 2) return
    val color = stroke.color.copy(alpha = stroke.alpha * 0.4f)
    val radius = stroke.width * 0.6f
    val rng = Random(42)
    for (i in 0 until stroke.points.size - 1) {
        val p0 = stroke.points[i]
        val p1 = stroke.points[i + 1]
        val steps = max(1, ((p1 - p0).getDistance() / 3f).toInt())
        for (s in 0..steps) {
            val t = s.toFloat() / max(1, steps)
            val cx = p0.x + (p1.x - p0.x) * t
            val cy = p0.y + (p1.y - p0.y) * t
            repeat(8) {
                val angle = rng.nextFloat() * 2f * kotlin.math.PI.toFloat()
                val dist = rng.nextFloat() * radius
                val dx = cos(angle.toDouble()).toFloat() * dist
                val dy = sin(angle.toDouble()).toFloat() * dist
                drawCircle(color, radius * 0.15f, Offset(cx + dx, cy + dy))
            }
        }
    }
}

private fun DrawScope.drawRainbowStroke(stroke: Stroke) {
    if (stroke.points.size < 2) return
    val cap = if (stroke.shape == BrushShape.ROUND) StrokeCap.Round else StrokeCap.Square
    val hueStep = 360f / max(1, stroke.points.size - 1)
    for (i in 0 until stroke.points.size - 1) {
        val hue = (stroke.startHue + hueStep * i) % 360f
        val color = Color.hsl(hue, 0.9f, 0.5f).copy(alpha = stroke.alpha)
        drawLine(color, stroke.points[i], stroke.points[i + 1],
            strokeWidth = stroke.width, cap = cap)
    }
}

private fun DrawScope.drawCalligraphyStroke(stroke: Stroke, density: Float) {
    if (stroke.points.size < 2) return
    val cap = StrokeCap.Round
    for (i in 0 until stroke.points.size - 1) {
        val dt = if (i < stroke.timestamps.size - 1) stroke.timestamps[i + 1] - stroke.timestamps[i] else 50L
        val dist = (stroke.points[i + 1] - stroke.points[i]).getDistance()
        val speed = if (dt > 0) dist / dt * 1000f else 0f
        val w = max(4f, min(stroke.width, stroke.width * 30f / max(1f, speed)))
        drawLine(stroke.color.copy(alpha = stroke.alpha), stroke.points[i], stroke.points[i + 1],
            strokeWidth = w, cap = cap)
    }
}

private fun Offset.getDistance(): Float = sqrt(x * x + y * y)

private fun Offset.minus(other: Offset): Offset = Offset(x - other.x, y - other.y)

@Composable
fun CanvasDrawing(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val strings = LocalAppStrings.current
    val strokes = remember { mutableStateListOf<Stroke>() }
    val currentStroke = remember { mutableStateOf<Stroke?>(null) }
    var currentColor by remember { mutableStateOf(Color.Black) }
    var currentSize by remember { mutableStateOf(8f) }
    var currentAlpha by remember { mutableFloatStateOf(1f) }
    var currentShape by remember { mutableStateOf(BrushShape.ROUND) }
    var currentMode by remember { mutableStateOf(BrushMode.NORMAL) }
    var isEraser by remember { mutableStateOf(false) }
    var showMoreColors by remember { mutableStateOf(false) }
    var showTooltip by remember { mutableStateOf<String?>(null) }

    val saveToGallery = {
        val saved = saveDrawing(context, strokes.toList(), 1080, 1920)
        showTooltip = if (saved) strings.cdSaved else strings.cdSaveFailed
    }
    // Android 8/9 need WRITE_EXTERNAL_STORAGE to insert into MediaStore;
    // API 29+ uses scoped storage and needs no permission.
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) saveToGallery() else showTooltip = strings.cdSaveFailed
    }

    LaunchedEffect(showTooltip) {
        if (showTooltip != null) { delay(2000); showTooltip = null }
    }

    val activeColor = if (isEraser) Color.White else currentColor
    val activeMode = if (isEraser) BrushMode.NORMAL else currentMode
    val activeAlpha = if (isEraser) 1f else currentAlpha

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).clickable(onClickLabel = strings.cdBack) { haptic.performIfEnabled(); onBack() },
                contentAlignment = Alignment.Center,
            ) {
                Text("\u2190", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.width(8.dp))
            Text("Canvas", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { isEraser = !isEraser; haptic.performIfEnabled() }) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (isEraser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(if (isEraser) "Erase" else "Draw", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(Color.White)
                .pointerInput(currentMode, currentShape, currentSize, activeColor, activeAlpha) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val s = Stroke(
                                color = activeColor,
                                width = if (isEraser) currentSize * 3f else currentSize,
                                points = mutableListOf(offset),
                                alpha = activeAlpha,
                                shape = currentShape,
                                mode = activeMode,
                                timestamps = mutableListOf(System.currentTimeMillis()),
                                startHue = (System.currentTimeMillis() % 360000).toFloat() / 1000f,
                            )
                            currentStroke.value = s
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentStroke.value?.points?.add(change.position)
                            currentStroke.value?.timestamps?.add(System.currentTimeMillis())
                        },
                        onDragEnd = {
                            currentStroke.value?.let { strokes.add(it) }
                            currentStroke.value = null
                        },
                    )
                },
        ) {
            for (stroke in strokes) {
                drawStroke(stroke, density)
            }
            currentStroke.value?.let { drawStroke(it, density) }
        }

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        ) {
            BrushMode.entries.forEach { mode ->
                val label = mode.name.lowercase().replaceFirstChar { it.uppercase() }
                val isActive = currentMode == mode && !isEraser
                Button(
                    onClick = { currentMode = mode; haptic.performIfEnabled() },
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    modifier = Modifier.height(28.dp),
                ) {
                    Text(label.take(4), style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BrushShape.entries.forEach { shape ->
                val isActive = currentShape == shape
                Button(
                    onClick = { currentShape = shape },
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    modifier = Modifier.height(28.dp),
                ) {
                    Text(shape.name, style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.width(4.dp))
            Text("Op", style = MaterialTheme.typography.labelSmall)
            Slider(
                value = currentAlpha,
                onValueChange = { currentAlpha = it },
                valueRange = 0.1f..1f,
                modifier = Modifier.weight(1f).height(24.dp),
            )
        }

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            for (color in presetColors.take(9)) {
                val isSelected = currentColor == color
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(color, if (currentShape == BrushShape.ROUND) CircleShape else RoundedCornerShape(3.dp))
                        .clickable { currentColor = color; showMoreColors = false },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (Color.Black == color || color == Color(0xFF9C27B0) || color == Color(0xFFE91E63)) Color.White else Color.Black,
                                    CircleShape
                                ),
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                    .clickable { showMoreColors = !showMoreColors },
                contentAlignment = Alignment.Center,
            ) {
                Text(if (showMoreColors) "\u25B2" else "\u25BC", style = MaterialTheme.typography.labelSmall)
            }
        }

        if (showMoreColors) {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                for (color in presetColors.drop(9)) {
                    val isSelected = currentColor == color
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(color, CircleShape)
                            .clickable { currentColor = color },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier.size(8.dp).background(
                                    if (color == Color(0xFFCDDC39) || color == Color(0xFFFFC107)) Color.Black else Color.White,
                                    CircleShape
                                ),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("Hue", style = MaterialTheme.typography.labelSmall)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clickable { /* hue picker taps handled below */ }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            val hue = (change.position.x / size.width * 360f).coerceIn(0f, 360f)
                            currentColor = Color.hsl(hue, 0.8f, 0.5f)
                        }
                    },
            ) {
                val steps = 36
                for (i in 0 until steps) {
                    val hue = i * 10f
                    val x0 = i.toFloat() / steps * size.width
                    val x1 = (i + 1).toFloat() / steps * size.width
                    drawRect(Color.hsl(hue, 0.8f, 0.5f), Offset(x0, 0f), androidx.compose.ui.geometry.Size(x1 - x0, size.height))
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        ) {
            for (size in brushSizeOptions) {
                val isSelected = currentSize == size
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                        .clickable { currentSize = size },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size((size / 50f * 24).dp.coerceAtLeast(4.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                CircleShape
                            ),
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Button(
                onClick = { if (strokes.isNotEmpty()) strokes.removeLast() },
                enabled = strokes.isNotEmpty(),
                contentPadding = ButtonDefaults.TextButtonContentPadding,
            ) {
                Icon(Icons.Filled.Undo, contentDescription = strings.cdUndo, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(2.dp))
                Text(strings.cdUndo)
            }
            Button(
                onClick = { strokes.clear() },
                enabled = strokes.isNotEmpty(),
                contentPadding = ButtonDefaults.TextButtonContentPadding,
            ) {
                Icon(Icons.Filled.Clear, contentDescription = strings.cdClear, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(2.dp))
                Text(strings.cdClear)
            }
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        saveToGallery()
                    } else {
                        storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                },
                enabled = strokes.isNotEmpty(),
                contentPadding = ButtonDefaults.TextButtonContentPadding,
            ) {
                Icon(Icons.Filled.Save, contentDescription = strings.cdSave, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(2.dp))
                Text(strings.cdSave)
            }
        }

        if (showTooltip != null) {
            Spacer(Modifier.height(2.dp))
            Text(
                showTooltip ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}
