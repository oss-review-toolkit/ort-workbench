package org.ossreviewtoolkit.workbench.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import java.io.File

import org.ossreviewtoolkit.workbench.utils.browseDirectory
import org.ossreviewtoolkit.workbench.utils.editFile
import org.ossreviewtoolkit.workbench.utils.openUrlInBrowser

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
        CompositionLocalProvider(LocalContentAlpha provides if (enabled) ContentAlpha.high else ContentAlpha.medium) {
            Text(text)
        }

        if (icon != null) {
            Icon(
                icon,
                contentDescription = "link",
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.size(16.dp)
            )
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
    Link(text, tooltip = file.path, icon = rememberVectorPainter(Icons.AutoMirrored.Default.OpenInNew)) {
        browseDirectory(file)
    }
}

@Composable
fun EditFileLink(text: String, file: File) {
    Link(text, tooltip = file.path, icon = rememberVectorPainter(Icons.AutoMirrored.Default.OpenInNew)) {
        editFile(file)
    }
}

@Composable
fun WebLink(text: String, url: String) {
    Link(text, tooltip = url, icon = rememberVectorPainter(Icons.AutoMirrored.Default.OpenInNew)) {
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
