package com.marc.videonest.ui.theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NestBlack = Color(0xFF0A0A0F)
val NestSurface = Color(0xFF13131A)
val NestElevated = Color(0xFF1E1E2A)
val NestViolet = Color(0xFF7C5CBF)
val NestVioletBright = Color(0xFF9D7FE0)
val NestWhite = Color(0xFFF0EEF8)
val NestMuted = Color(0xFF7A7A9A)
val NestDanger = Color(0xFFE05C5C)
val YoutubeRed = Color(0xFFFF0000)
val TikTokCyan = Color(0xFF69C9D0)
val InstagramPurple = Color(0xFFE1306C)
val TwitterBlue = Color(0xFF1DA1F2)
val FacebookBlue = Color(0xFF1877F2)

private val DarkColors = darkColorScheme(
    primary = NestViolet, onPrimary = NestWhite,
    background = NestBlack, onBackground = NestWhite,
    surface = NestSurface, onSurface = NestWhite,
    surfaceVariant = NestElevated, onSurfaceVariant = NestMuted,
    error = NestDanger, onError = NestWhite,
)

@Composable
fun VideoNestTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
