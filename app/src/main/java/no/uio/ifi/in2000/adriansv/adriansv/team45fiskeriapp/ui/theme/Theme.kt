@file:Suppress("DEPRECATION")

package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Marine farger for fiskeriapp
private val DarkBlue = Color(0xFF0D47A1)
private val LightBlue = Color(0xFF1976D2)
private val NavyBlue = Color(0xFF002171)
private val SkyBlue = Color(0xFF42A5F5)
private val TurquoiseBlue = Color(0xFF00BCD4)
private val DarkSurface = ProfileDarkBackground
private val DarkBackground = ProfileDarkBackground
private val LightSurface = ProfileLightBackground
private val LightBackground = ProfileLightBackground

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlue,
    onPrimary = Color.White,
    primaryContainer = NavyBlue,
    onPrimaryContainer = SkyBlue,
    secondary = TurquoiseBlue,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF003644),
    onSecondaryContainer = TurquoiseBlue,
    tertiary = Color(0xFF4DD0E1),
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = ProfileDarkSurface,
    onSurfaceVariant = Color.White.copy(alpha = 0.7f),
    error = Color(0xFFCF6679),
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = DarkBlue,
    onPrimary = Color.White,
    primaryContainer = LightBlue,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF0288D1),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFBBDEFB),
    onSecondaryContainer = Color(0xFF01579B),
    tertiary = Color(0xFF0097A7),
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = Color.Black,
    surface = LightSurface,
    onSurface = Color.Black,
    surfaceVariant = ProfileLightSurface,
    onSurfaceVariant = Color.Black.copy(alpha = 0.7f),
    error = Color(0xFFB00020),
    onError = Color.White
)

@Composable
fun Team45FiskeriAppTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}