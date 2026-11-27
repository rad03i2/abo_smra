package com.abosmra.app.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Navy = Color(0xFF0A2A5E)
val Blue = Color(0xFF1456B8)
val BlueSoft = Color(0xFFDCE8FB)
val Flame = Color(0xFFFF7A1A)
val FlameSoft = Color(0xFFFFE8D1)
val FlameLight = Color(0xFFFFB347)
val Green = Color(0xFF16A34A)
val GreenSoft = Color(0xFFDDF5E6)
val Red = Color(0xFFE11D48)
val RedSoft = Color(0xFFFFE1E7)
val Amber = Color(0xFFF59E0B)
val AmberSoft = Color(0xFFFFF1CC)
val Teal = Color(0xFF0E9AA7)
val TealSoft = Color(0xFFD6F3F5)
val PageBg = Color(0xFFF2F5FA)
val InkDark = Color(0xFF0F172A)
val InkMid = Color(0xFF52607A)
val Line = Color(0xFFE3E9F3)

private val scheme = lightColorScheme(
    primary = Blue,
    onPrimary = Color.White,
    primaryContainer = BlueSoft,
    onPrimaryContainer = Navy,
    secondary = Flame,
    onSecondary = Color.White,
    secondaryContainer = FlameSoft,
    onSecondaryContainer = Color(0xFF7A3300),
    tertiary = Teal,
    background = PageBg,
    onBackground = InkDark,
    surface = Color.White,
    onSurface = InkDark,
    surfaceVariant = Color(0xFFE9EEF6),
    onSurfaceVariant = InkMid,
    outline = Color(0xFFC5CFE0),
    outlineVariant = Line,
    error = Red
)

private val typography = Typography(
    headlineMedium = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold),
    titleLarge = TextStyle(fontSize = 21.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    bodySmall = TextStyle(fontSize = 12.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium)
)

private val shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AboSmraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = scheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
