package tech.aranda.myKyBCombineDroid.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min
import kotlin.math.sqrt

val Cyan = Color(0xFF00E5FF)
val DarkBg = Color(0xFF050A12)

@Composable
fun JoystickView(
    label: String,
    verticalOnly: Boolean = false,
    horizontalOnly: Boolean = false,
    onChanged: (x: Double, y: Double) -> Unit,
    onReleased: () -> Unit,
    modifier: Modifier = Modifier
) {
    var stickOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    val size = 140.dp
    val stickRadius = 24f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = label,
            color = Cyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .size(size)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { pos ->
                            isDragging = true
                            val center = Offset(this.size.width / 2f, this.size.height / 2f)
                            var delta = pos - center
                            if (verticalOnly) delta = Offset(0f, delta.y)
                            if (horizontalOnly) delta = Offset(delta.x, 0f)
                            val maxDist = (this.size.width / 2f) - stickRadius
                            val dist = sqrt(delta.x * delta.x + delta.y * delta.y)
                            if (dist > maxDist) delta = delta / dist * maxDist
                            stickOffset = delta
                            onChanged(
                                (delta.x / maxDist).toDouble(),
                                (delta.y / maxDist).toDouble()
                            )
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val center = Offset(this.size.width / 2f, this.size.height / 2f)
                            var delta = change.position - center
                            if (verticalOnly) delta = Offset(0f, delta.y)
                            if (horizontalOnly) delta = Offset(delta.x, 0f)
                            val maxDist = (this.size.width / 2f) - stickRadius
                            val dist = sqrt(delta.x * delta.x + delta.y * delta.y)
                            if (dist > maxDist) delta = delta / dist * maxDist
                            stickOffset = delta
                            onChanged(
                                (delta.x / maxDist).toDouble(),
                                (delta.y / maxDist).toDouble()
                            )
                        },
                        onDragEnd = {
                            isDragging = false
                            stickOffset = Offset.Zero
                            onReleased()
                        },
                        onDragCancel = {
                            isDragging = false
                            stickOffset = Offset.Zero
                            onReleased()
                        }
                    )
                }
        ) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val outerRadius = size.toPx() / 2f

            // Outer ring fill
            drawCircle(
                color = Cyan.copy(alpha = 0.1f),
                radius = outerRadius,
                center = center
            )

            // Outer ring border
            drawCircle(
                color = Cyan.copy(alpha = 0.4f),
                radius = outerRadius - 1f,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // Cross lines
            drawLine(
                color = Cyan.copy(alpha = 0.2f),
                start = Offset(center.x, center.y - outerRadius + 8f),
                end = Offset(center.x, center.y + outerRadius - 8f),
                strokeWidth = 1f
            )
            drawLine(
                color = Cyan.copy(alpha = 0.2f),
                start = Offset(center.x - outerRadius + 8f, center.y),
                end = Offset(center.x + outerRadius - 8f, center.y),
                strokeWidth = 1f
            )

            // Stick
            val stickPos = center + stickOffset
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isDragging)
                        listOf(Cyan, Color(0xFF0088AA))
                    else
                        listOf(Color(0xFF0088CC), Color(0xFF004466)),
                    center = stickPos,
                    radius = stickRadius
                ),
                radius = stickRadius,
                center = stickPos
            )

            // Stick border
            drawCircle(
                color = Cyan.copy(alpha = if (isDragging) 1f else 0.6f),
                radius = stickRadius,
                center = stickPos,
                style = Stroke(width = 2f)
            )

            // Center dot
            drawCircle(
                color = Cyan.copy(alpha = 0.8f),
                radius = 4f,
                center = stickPos
            )
        }
    }
}
