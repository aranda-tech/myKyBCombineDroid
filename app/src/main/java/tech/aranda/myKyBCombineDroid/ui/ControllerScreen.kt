package tech.aranda.myKyBCombineDroid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.aranda.myKyBCombineDroid.ble.BLEManager
import kotlin.math.abs

@Composable
fun ControllerScreen(
    ble: BLEManager,
    onDisconnect: () -> Unit
) {
    var driveY by remember { mutableDoubleStateOf(0.0) }
    var steerX by remember { mutableDoubleStateOf(0.0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top bar
            TopBar(
                mod1 = ble.protocol.mod1,
                mod2 = ble.protocol.mod2,
                onDisconnect = onDisconnect
            )

            // Speed bars
            SpeedBars(driveY = driveY, steerX = steerX)

            Spacer(modifier = Modifier.weight(1f))

            // Joysticks
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left joystick — drive
                JoystickView(
                    label = "DRIVE",
                    verticalOnly = true,
                    onChanged = { _, y ->
                        driveY = y
                        ble.sendDrive(driveY = y, steerX = steerX)
                    },
                    onReleased = {
                        driveY = 0.0
                        ble.sendDrive(driveY = 0.0, steerX = steerX)
                    }
                )

                // Center label
                CenterLabel(driveY = driveY, steerX = steerX)

                // Right joystick — steer
                JoystickView(
                    label = "STEER",
                    horizontalOnly = true,
                    onChanged = { x, _ ->
                        steerX = x
                        ble.sendDrive(driveY = driveY, steerX = x)
                    },
                    onReleased = {
                        steerX = 0.0
                        ble.sendDrive(driveY = driveY, steerX = 0.0)
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom bar
            BottomBar(mod1 = ble.protocol.mod1, mod2 = ble.protocol.mod2)
        }
    }
}

@Composable
fun TopBar(mod1: Byte, mod2: Byte, onDisconnect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .border(
                width = 0.dp,
                color = Color.Transparent
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Connection indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(8.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = Color(0xFF00FF88)
            ) {}
            Text(
                "JX-APP-A",
                color = Color(0xFF00FF88),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            "KY BLOCKS K96234",
            color = Cyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 3.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        // Disconnect button
        TextButton(
            onClick = onDisconnect,
            border = androidx.compose.foundation.BorderStroke(
                1.dp, Color(0xFFFF3366).copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(2.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                "DISC",
                color = Color(0xFFFF3366),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }

    Divider(color = Cyan.copy(alpha = 0.15f), thickness = 1.dp)
}

@Composable
fun SpeedBars(driveY: Double, steerX: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SpeedBar(label = "L", value = driveY, positive = driveY < 0)
        SpeedBar(label = "R", value = steerX, positive = steerX > 0)
    }
}

@Composable
fun SpeedBar(label: String, value: Double, positive: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(label, color = Cyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)

        Box(
            modifier = Modifier
                .width(80.dp)
                .height(4.dp)
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(abs(value).toFloat().coerceIn(0f, 1f))
                    .align(if (positive) Alignment.CenterEnd else Alignment.CenterStart)
                    .background(Cyan, RoundedCornerShape(2.dp))
            )
        }

        Text(
            "${(abs(value) * 100).toInt()}%",
            color = Cyan,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(32.dp)
        )
    }
}

@Composable
fun CenterLabel(driveY: Double, steerX: Double) {
    val label = buildString {
        if (driveY < -0.1) append("FWD")
        else if (driveY > 0.1) append("REV")
        if (steerX > 0.1) append("+R")
        else if (steerX < -0.1) append("+L")
        if (isEmpty()) append("IDLE")
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            color = Cyan,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 3.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Divider(
            modifier = Modifier
                .width(1.dp)
                .height(40.dp),
            color = Cyan.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun BottomBar(mod1: Byte, mod2: Byte) {
    Divider(color = Cyan.copy(alpha = 0.15f), thickness = 1.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "MODULE: %02X %02X".format(mod1.toInt() and 0xFF, mod2.toInt() and 0xFF),
            color = Color.White.copy(alpha = 0.25f),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )
    }
}
