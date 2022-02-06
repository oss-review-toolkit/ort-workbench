package org.ossreviewtoolkit.workbench.util

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

import org.ossreviewtoolkit.workbench.theme.LightGray

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Link(text: String, tooltip: String? = null, icon: Painter? = null, enabled: Boolean = true, onClick: () -> Unit) {
    if (tooltip != null) {
        TooltipArea(tooltip = { Tooltip(tooltip) }) {
            LinkContent(text, icon, enabled, onClick)
        }
    } else {
        LinkContent(text, icon, enabled, onClick)
    }
}

@Composable
private fun LinkContent(text: String, icon: Painter?, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.clickable { if (enabled) onClick() },
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = "link", tint = MaterialTheme.colors.primary)
        }

        Text(text, color = if (enabled) MaterialTheme.colors.primary else LightGray)
    }
}

@Composable
@Preview
private fun LinkPreview() {
    Preview {
        Column {
            Link("Enabled link") {}
            Link("Disabled link", enabled = false) {}
        }
    }
}

@Composable
fun WebLink(text: String, url: String) {
    Link(text, tooltip = url, icon = painterResource(MaterialIcon.OPEN_IN_NEW.resource)) {
        openUrlInBrowser(url)
    }
}

@Composable
@Preview
private fun WebLinkPreview() {
    Preview {
        WebLink("Web Link", "")
    }
}
