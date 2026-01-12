package org.ossreviewtoolkit.workbench.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview

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
            icon = rememberVectorPainter(Icons.Default.BugReport),
            text = "Issues"
        )
    }
}
