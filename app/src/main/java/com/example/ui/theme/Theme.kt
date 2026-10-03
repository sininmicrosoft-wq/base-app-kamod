package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BaseBlue,
    onPrimary = Color.White,
    primaryContainer = BaseBlueContainer,
    onPrimaryContainer = BaseCyan,
    secondary = BaseCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF00384D),
    onSecondaryContainer = BaseCyan,
    tertiary = BaseTeal,
    onTertiary = Color.Black,
    background = BaseBackgroundDark,
    onBackground = BaseTextPrimary,
    surface = BaseSurfaceDark,
    onSurface = BaseTextPrimary,
    surfaceVariant = BaseSurfaceVariantDark,
    onSurfaceVariant = BaseTextSecondary,
    outline = BaseBorderDark,
    error = BaseRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BaseBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE6FF),
    onPrimaryContainer = BaseBlueDark,
    secondary = Color(0xFF007A99),
    onSecondary = Color.White,
    tertiary = Color(0xFF00875A),
    background = BaseBackgroundLight,
    onBackground = Color(0xFF0F172A),
    surface = BaseSurfaceLight,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = BaseSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF475569),
    outline = BaseBorderLight,
    error = BaseRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep Base brand identity prominent
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme // Base dark tech theme looks premium
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
