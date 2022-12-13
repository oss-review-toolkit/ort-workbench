package org.ossreviewtoolkit.workbench.util

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

import java.io.File

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

        CompositionLocalProvider(LocalContentAlpha provides if (enabled) ContentAlpha.high else ContentAlpha.medium) {
            Text(text)
        }
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
fun BrowseDirectoryLink(text: String, file: File) {
    Link(text, tooltip = file.path, icon = painterResource(MaterialIcon.OPEN_IN_NEW.resource)) {
        browseDirectory(file)
    }
}

@Composable
fun EditFileLink(text: String, file: File) {
    Link(text, tooltip = file.path, icon = painterResource(MaterialIcon.OPEN_IN_NEW.resource)) {
        editFile(file)
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
