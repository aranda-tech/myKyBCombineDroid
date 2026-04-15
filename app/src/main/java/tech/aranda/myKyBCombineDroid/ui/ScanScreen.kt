package tech.aranda.myKyBCombineDroid.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.aranda.myKyBCombineDroid.ble.HubState

@Composable
fun ScanScreen(
    state: HubState,
    log: List<String>,
    onScan: () -> Unit
) {
    val isScanning = state is HubState.Scanning || state is HubState.Connecting

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val displayLog = if (log.isEmpty()) listOf("Waiting for Bluetooth...") else log

    // Landscape layout: left column (branding + button) | right column (log)
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // LEFT — branding + status button
        Column(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Pulsing icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(pulse)
                    .border(1.5.dp, Cyan.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "((·))",
                    color = Cyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "KY BLOCKS",
                color = Cyan,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "K96234 CONTROLLER",
                color = Cyan.copy(alpha = 0.6f),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Status / scan button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp),
                color = Cyan.copy(alpha = 0.08f),
                border = BorderStroke(1.5.dp, Cyan.copy(alpha = 0.6f)),
                onClick = { if (!isScanning) onScan() }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = Cyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = state.description,
                        color = stateColor(state),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        // RIGHT — log
        Column(
            modifier = Modifier
                .weight(0.55f)
                .fillMaxHeight()
        ) {
            Text(
                text = "LOG",
                color = Cyan.copy(alpha = 0.4f),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF080E1A), RoundedCornerShape(8.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(displayLog) { line ->
                        Text(
                            text = line,
                            color = Cyan.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }
    }
}

fun stateColor(state: HubState) = when (state) {
    is HubState.Connected -> Color(0xFF00FF88)
    is HubState.Error -> Color(0xFFFF3366)
    is HubState.Scanning, is HubState.Connecting -> Cyan
    else -> Color.White.copy(alpha = 0.5f)
}