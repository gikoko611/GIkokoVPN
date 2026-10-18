package com.v2ray.ang.ui.compose

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.v2ray.ang.AppConfig
import com.v2ray.ang.handler.MmkvManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// G.I ko ko vpn - cyber green palette
val CyberGreen = Color(0xFF2DFF8A)
val CyberLime = Color(0xFFB6FF3D)
val CyberTeal = Color(0xFF12B886)
val CyberBg = Color(0xFF040B07)
val CyberPanel = Color(0xFF0A1610)

private val LightColor = lightColorScheme(
    primary = Color(0xFF0A8A4A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBFF5D6),
    onPrimaryContainer = Color(0xFF00210F),
    secondary = Color(0xFF5F8F00),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDDF5A6),
    onSecondaryContainer = Color(0xFF1A2600),
    tertiary = Color(0xFF0B7A63),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFA0F2D0),
    onTertiaryContainer = Color(0xFF00201A),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFEEF7F1),
    onBackground = Color(0xFF0A2016),
    surface = Color(0xFFF4FBF6),
    onSurface = Color(0xFF0A2016),
    surfaceVariant = Color(0xFFDFF0E6),
    onSurfaceVariant = Color(0xFF4A6B58),
    outline = Color(0xFF6E9A82),
    outlineVariant = Color(0xFFAECDBB),
    inverseSurface = Color(0xFF0A2016),
    inverseOnSurface = Color(0xFFEEF7F1),
    inversePrimary = Color(0xFF2DFF8A),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFF0A8A4A),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1F9F4),
    surfaceContainer = Color(0xFFE9F4ED),
    surfaceContainerHigh = Color(0xFFE0EFE6),
    surfaceContainerHighest = Color(0xFFD7E9DE),
)

private val DarkColor = darkColorScheme(
    primary = Color(0xFF2DFF8A),
    onPrimary = Color(0xFF031A0E),
    primaryContainer = Color(0xFF0F3A22),
    onPrimaryContainer = Color(0xFFB6FFD2),
    secondary = Color(0xFFB6FF3D),
    onSecondary = Color(0xFF1A2600),
    secondaryContainer = Color(0xFF2E4410),
    onSecondaryContainer = Color(0xFFDDFFA0),
    tertiary = Color(0xFF12B886),
    onTertiary = Color(0xFF00231A),
    tertiaryContainer = Color(0xFF08402F),
    onTertiaryContainer = Color(0xFFA0F2D0),
    error = Color(0xFFFF5D6C),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF040B07),
    onBackground = Color(0xFFDCFCE9),
    surface = Color(0xFF071410),
    onSurface = Color(0xFFDCFCE9),
    surfaceVariant = Color(0xFF10231A),
    onSurfaceVariant = Color(0xFF7FB598),
    outline = Color(0xFF2A6B4A),
    outlineVariant = Color(0xFF1B4030),
    inverseSurface = Color(0xFFDCFCE9),
    inverseOnSurface = Color(0xFF04120B),
    inversePrimary = Color(0xFF0A8A4A),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFF2DFF8A),
    surfaceContainerLowest = Color(0xFF030906),
    surfaceContainerLow = Color(0xFF08130E),
    surfaceContainer = Color(0xFF0A1610),
    surfaceContainerHigh = Color(0xFF10231A),
    surfaceContainerHighest = Color(0xFF163024),
)

// Semantic Colors
val colorPing = Color(0xFF2DFF8A) // Green
val colorPingRed = Color(0xFFFF5D6C) // Red
val colorConfigType = Color(0xFFB6FF3D) // Lime
val colorFabActive = Color(0xFF2DFF8A) // Neon green
val colorFabInactiveLight = Color(0xFF7FA38F) // Gray green
val colorFabInactiveDark = Color(0xFF2A4A3A) // Dark gray green
val dividerColorLight = Color(0xFFAECDBB) // Light
val dividerColorDark = Color(0xFF1B4030) // Dark

// Toast Colors 85%
val toastNormalBgLight = Color(0xD9353A3E) // Dark Gray
val toastNormalBgDark = Color(0xD94A4F54) // Darker Gray
val toastSuccessBg = Color(0xD9388E3C) // Green
val toastErrorBg = Color(0xD9D50000) // Red
val toastInfoBg = Color(0xD90B7A63) // Teal
val toastIconCircleBg = Color(0x33FFFFFF) // Semi-transparent White
val toastTextColor = Color.White // White

object ThemeManager {
    // Default is dark (mode "2") and fixed colors (no dynamic color) so the cyber green theme shows.
    private val _themeMode = MutableStateFlow(
        MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "2") ?: "2"
    )
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _dynamicColorEnabled = MutableStateFlow(
        MmkvManager.decodeSettingsBool(AppConfig.PREF_DYNAMIC_COLOR, false)
    )
    val dynamicColorEnabled: StateFlow<Boolean> = _dynamicColorEnabled.asStateFlow()

    fun setThemeMode(mode: String) {
        MmkvManager.encodeSettings(AppConfig.PREF_UI_MODE_NIGHT, mode)
        _themeMode.value = mode
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        MmkvManager.encodeSettings(AppConfig.PREF_DYNAMIC_COLOR, enabled)
        _dynamicColorEnabled.value = enabled
    }

    fun refresh() {
        _themeMode.value =
            MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "2") ?: "2"
        _dynamicColorEnabled.value =
            MmkvManager.decodeSettingsBool(AppConfig.PREF_DYNAMIC_COLOR, false)
    }
}

@Composable
fun resolveDarkTheme(): Boolean {
    val mode by ThemeManager.themeMode.collectAsState()
    return when (mode) {
        "1" -> false
        "2" -> true
        else -> isSystemInDarkTheme()
    }
}

val LocalDarkTheme = compositionLocalOf { false }

@Composable
fun AppTheme(
    darkTheme: Boolean = resolveDarkTheme(),
    content: @Composable () -> Unit
) {
    val dynamicColor by ThemeManager.dynamicColorEnabled.collectAsState()
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColor
        else -> LightColor
    }
    val snackbarController = rememberAppSnackbarController()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val window = activity.window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalAppSnackbar provides snackbarController
    ) {
        MaterialTheme(
            colorScheme = colorScheme
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AppSnackbarBridge(controller = snackbarController)
                content()
                AppSnackbarHost(hostState = snackbarController.hostState)
            }
        }
    }
}
