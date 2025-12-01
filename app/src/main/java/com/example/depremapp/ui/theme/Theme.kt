package com.example.depremapp.ui.theme

import android.app.Activity
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
    primary = DarkOrange,              // Ana vurgu rengi
    onPrimary = Color.Black,          // Primary üzerindeki metin
    primaryContainer = DarkCardBackground,
    onPrimaryContainer = DarkTextPrimary,

    secondary = DarkAmber,            // İkincil vurgu
    onSecondary = Color.Black,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = DarkTextPrimary,

    tertiary = InfoBlue,              // Bilgi rengi
    onTertiary = Color.White,

    error = ErrorDark,                // Hata rengi
    onError = Color.White,
    errorContainer = Color(0xFF5A1C1C),
    onErrorContainer = ErrorDark,

    background = DarkGray,            // Ana arka plan
    onBackground = DarkTextPrimary,

    surface = DarkSurface,            // Yüzeyler (Card, vb.)
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCardBackground,
    onSurfaceVariant = DarkTextSecondary,

    outline = DarkTextSecondary,
    outlineVariant = Color(0xFF444444)
)

private val LightColorScheme = lightColorScheme(
    primary = LightOrange,            // Ana vurgu rengi
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0B2),
    onPrimaryContainer = Color(0xFF4E2E00),

    secondary = LightAmber,           // İkincil vurgu
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFECB3),
    onSecondaryContainer = Color(0xFF4E3500),

    tertiary = InfoBlue,              // Bilgi rengi
    onTertiary = Color.White,

    error = ErrorLight,               // Hata rengi
    onError = Color.White,
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = ErrorLight,

    background = LightGray,           // Ana arka plan
    onBackground = LightTextPrimary,

    surface = LightCardBackground,    // Yüzeyler (Card, vb.)
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurface,
    onSurfaceVariant = LightTextSecondary,

    outline = Color(0xFFBDBDBD),
    outlineVariant = Color(0xFFE0E0E0)
)

@Composable
fun DepremAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}