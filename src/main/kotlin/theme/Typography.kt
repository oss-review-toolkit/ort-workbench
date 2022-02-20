@file:Suppress("MagicNumber")

package org.ossreviewtoolkit.workbench.theme

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

import org.ossreviewtoolkit.workbench.util.Preview

val Typography = Typography(
    h1 = TextStyle(
        fontWeight = FontWeight.Light,
        fontSize = 48.sp,
        letterSpacing = (-1.5).sp
    ),
    h2 = TextStyle(
        fontWeight = FontWeight.Light,
        fontSize = 32.sp,
        letterSpacing = (-0.5).sp
    ),
    h3 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        letterSpacing = 0.sp
    ),
    h4 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        letterSpacing = 0.25.sp
    ),
    h5 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.sp
    ),
    h6 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.15.sp
    ),
    subtitle1 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        letterSpacing = 0.15.sp
    ),
    subtitle2 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.1.sp
    ),
    body1 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        letterSpacing = 0.5.sp
    ),
    body2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.25.sp
    ),
    button = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 1.25.sp
    ),
    caption = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        letterSpacing = 0.4.sp
    ),
    overline = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        letterSpacing = 1.5.sp
    )
)

@Composable
@Preview
private fun TypographyPreview() {
    Preview {
        Column {
            Text("Header 1", style = Typography.h1)
            Text("Header 2", style = Typography.h2)
            Text("Header 3", style = Typography.h3)
            Text("Header 4", style = Typography.h4)
            Text("Header 5", style = Typography.h5)
            Text("Header 6", style = Typography.h6)
            Text("Subtitle 1", style = Typography.subtitle1)
            Text("Subtitle 2", style = Typography.subtitle2)
            Text("Body 1", style = Typography.body1)
            Text("Body 2", style = Typography.body2)
            Text("Button", style = Typography.button)
            Text("Caption", style = Typography.caption)
            Text("Overline", style = Typography.overline)
        }
    }
}
