package org.ossreviewtoolkit.workbench.composables

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource

import org.ossreviewtoolkit.workbench.utils.MaterialIcon

private const val ICON_SCALE = 0.6f

@Composable
fun IconText(
    icon: Painter,
    text: String,
    contentDescription: String? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            modifier = Modifier.scale(ICON_SCALE)
        )
        Text(text)
    }
}

@Composable
@Preview
private fun IconTextPreview() {
    Preview {
        IconText(
            icon = painterResource(MaterialIcon.BUG_REPORT.resource),
            text = "Issues"
        )
    }
}
