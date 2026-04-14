package tech.aranda.myKyBCombineDroid.ui

import androidx.compose.animation.core.*
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

    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(pulse)
                    .border(2.dp, Cyan.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("📡", fontSize = 32.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                "KY BLOCKS",
                color = Cyan,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 8.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "K96234 CONTROLLER",
                color = Cyan.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .border(1.dp, stateColor(state).copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        color = Cyan,
                        strokeWidth = 2.dp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .border(0.dp, Color.Transparent, CircleShape)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = CircleShape,
                            color = stateColor(state)
                        ) {}
                    }
                }
                Text(
                    state.description,
                    color = stateColor(state),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Scan button
            if (!isScanning) {
                Button(
                    onClick = onScan,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cyan.copy(alpha = 0.1f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Cyan),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "SCAN FOR HUB",
                        color = Cyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 3.sp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Log
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                color = Color.White.copy(alpha = 0.02f),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, Color.White.copy(alpha = 0.08f)
                )
            ) {
                LazyColumn(
                    modifier = Modifier.padding(12.dp),
                    reverseLayout = false
                ) {
                    items(log) { line ->
                        Text(
                            line,
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

fun stateColor(state: HubState) = when (state) {
    is HubState.Connected -> Color(0xFF00FF88)
    is HubState.Error -> Color(0xFFFF3366)
    is HubState.Scanning, is HubState.Connecting -> Cyan
    else -> Color.White.copy(alpha = 0.4f)
}
