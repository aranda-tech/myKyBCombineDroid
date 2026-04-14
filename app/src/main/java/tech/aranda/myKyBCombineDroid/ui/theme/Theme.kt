package tech.aranda.myKyBCombineDroid.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00E5FF),
    background = Color(0xFF050A12),
    surface = Color(0xFF050A12),
    onPrimary = Color(0xFF050A12),
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun KYBlocksTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
