package org.ossreviewtoolkit.workbench.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumTouchTargetEnforcement
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

import org.ossreviewtoolkit.workbench.model.WorkbenchTheme

private val DarkColorPalette = darkColors(
    primary = Blue,
    primaryVariant = DarkBlue,
    secondary = Teal,
    secondaryVariant = DarkTeal,
    background = Gray,
    surface = DarkGray,
    error = Red,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White
)

private val LightColorPalette = lightColors(
    primary = LightBlue,
    primaryVariant = Blue,
    secondary = LightTeal,
    secondaryVariant = Teal,
    background = VeryLightGray,
    surface = Color.White,
    error = Red,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.White
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OrtWorkbenchTheme(theme: WorkbenchTheme, content: @Composable () -> Unit) {
    val colors = if (theme == WorkbenchTheme.DARK || (theme == WorkbenchTheme.AUTO && isSystemInDarkTheme())) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    CompositionLocalProvider(
        LocalMinimumTouchTargetEnforcement provides false,
    ) {
        MaterialTheme(
            colors = colors,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
