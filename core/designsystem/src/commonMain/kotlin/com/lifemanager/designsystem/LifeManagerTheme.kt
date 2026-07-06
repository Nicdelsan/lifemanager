package com.lifemanager.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LmLightColors = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE7FF),
    onPrimaryContainer = Color(0xFF102A56),
    secondary = Color(0xFF3F6B57),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCEBE2),
    onSecondaryContainer = Color(0xFF102419),
    tertiary = Color(0xFF9A5A17),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE2BD),
    onTertiaryContainer = Color(0xFF321A03),
    background = Color(0xFFFAFAF8),
    onBackground = Color(0xFF1F1F1D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1F1F1D),
    surfaceVariant = Color(0xFFF6F5F1),
    onSurfaceVariant = Color(0xFF3F3D38),
    outline = Color(0xFFD8D6D0),
    outlineVariant = Color(0xFFE8E6E0),
    inverseSurface = Color(0xFF2D2C29),
    inverseOnSurface = Color(0xFFF5F3EE),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD4),
    onErrorContainer = Color(0xFF410002),
)

private val LmDarkColors = darkColorScheme(
    primary = Color(0xFF9DBBFF),
    onPrimary = Color(0xFF12325F),
    primaryContainer = Color(0xFF1D4C8E),
    onPrimaryContainer = Color(0xFFDCE7FF),
    secondary = Color(0xFFAFCFBB),
    onSecondary = Color(0xFF1B3528),
    secondaryContainer = Color(0xFF294D3B),
    onSecondaryContainer = Color(0xFFDCEBE2),
    tertiary = Color(0xFFF1C28A),
    onTertiary = Color(0xFF4D2B00),
    tertiaryContainer = Color(0xFF6E3F08),
    onTertiaryContainer = Color(0xFFFFE2BD),
    background = Color(0xFF171716),
    onBackground = Color(0xFFE7E4DD),
    surface = Color(0xFF20201E),
    onSurface = Color(0xFFE7E4DD),
    surfaceVariant = Color(0xFF2B2B28),
    onSurfaceVariant = Color(0xFFC9C6BF),
    outline = Color(0xFF4E4D49),
    outlineVariant = Color(0xFF393834),
    inverseSurface = Color(0xFFE7E4DD),
    inverseOnSurface = Color(0xFF20201E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD4),
)

private val LmTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 42.sp,
        lineHeight = 50.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)

private val LmShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
)

@Composable
fun LifeManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) LmDarkColors else LmLightColors,
        typography = LmTypography,
        shapes = LmShapes,
        content = content,
    )
}
