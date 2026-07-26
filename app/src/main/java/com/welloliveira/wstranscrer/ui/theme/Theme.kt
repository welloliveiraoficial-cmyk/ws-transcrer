package com.welloliveira.wstranscrer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// O app so tem modo escuro (por design), entao ignoramos o tema do sistema.
private val WsDarkColors = darkColorScheme(
    primary = Sky,
    onPrimary = Bg,
    secondary = Deep,
    background = Bg,
    onBackground = Ink,
    surface = Panel2,
    onSurface = Ink,
    surfaceVariant = Panel2,
    onSurfaceVariant = InkDim,
    error = Danger
)

@Composable
fun WsTranscrerTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as? android.app.Activity)?.window
        window?.let {
            it.statusBarColor = Bg.toArgb()
            it.navigationBarColor = Bg.toArgb()
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = WsDarkColors,
        typography = WsTypography,
        content = content
    )
}
