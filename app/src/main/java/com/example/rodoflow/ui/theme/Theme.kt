package com.example.rodoflow.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val RodoDarkColorScheme = darkColorScheme(
    primary = RodoFlowBlue,
    onPrimary = RodoFlowBlack,
    primaryContainer = RodoFlowBlueMuted,
    onPrimaryContainer = RodoFlowWhite,

    secondary = RodoFlowNavyElevated,
    onSecondary = RodoFlowWhite,
    secondaryContainer = RodoFlowNavy,
    onSecondaryContainer = Color(0xFFE2EAF3),

    tertiary = Color(0xFF5EADFF),
    onTertiary = RodoFlowBlack,

    background = RodoFlowBlack,
    onBackground = RodoFlowWhite,

    surface = RodoFlowSurfaceDark,
    onSurface = RodoFlowWhite,
    surfaceVariant = RodoFlowNavy,
    onSurfaceVariant = Color(0xFFA8BFD4),

    outline = Color(0xFF3D556E),
    outlineVariant = Color(0xFF2A3D52),

    error = Color(0xFFFF5449),
    onError = RodoFlowWhite,
)

private val RodoLightColorScheme = lightColorScheme(
    primary = RodoFlowBlueMuted,
    onPrimary = RodoFlowWhite,
    primaryContainer = Color(0xFFCDE9FF),
    onPrimaryContainer = Color(0xFF004168),

    secondary = RodoFlowNavyElevated,
    onSecondary = RodoFlowWhite,
    secondaryContainer = Color(0xFFD4E8F7),
    onSecondaryContainer = Color(0xFF0F1F2F),

    tertiary = RodoFlowBlue,
    onTertiary = RodoFlowWhite,

    background = Color(0xFFF1F7FC),
    onBackground = RodoFlowBlack,

    surface = RodoFlowWhite,
    onSurface = Color(0xFF0F1F2F),
    surfaceVariant = Color(0xFFDDEAF5),
    onSurfaceVariant = Color(0xFF3F5366),

    outline = Color(0xFF6B8299),
    outlineVariant = Color(0xFFBFCEDC),

    error = Color(0xFFBA1A1A),
    onError = RodoFlowWhite,
)

@Composable
fun RodoFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    /** Desligado por defeito para manter a paleta da marca visível sem wallpaper do sistema. */
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> RodoDarkColorScheme
        else -> RodoLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
