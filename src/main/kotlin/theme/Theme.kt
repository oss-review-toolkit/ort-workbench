package org.ossreviewtoolkit.workbench.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

import org.ossreviewtoolkit.workbench.model.WorkbenchTheme

private val DarkColorPalette = darkColors(
    primary = Blue,
    primaryVariant = DarkBlue,
    secondary = Teal,
    secondaryVariant = DarkTeal,
    background = DarkGray,
    surface = DarkGray,
    error = Red,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White
)

private val LightColorPalette = lightColors(
    primary = LightBlue,
    primaryVariant = Blue,
    secondary = LightTeal,
    secondaryVariant = Teal,
    background = Color.White,
    surface = Color.White,
    error = Red,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.White
)

@Composable
fun OrtWorkbenchTheme(theme: WorkbenchTheme, content: @Composable () -> Unit) {
    val colors = if (theme == WorkbenchTheme.DARK || (theme == WorkbenchTheme.AUTO && isSystemInDarkTheme())) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
