package org.ossreviewtoolkit.workbench.util

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import org.ossreviewtoolkit.workbench.theme.LightGray

@Composable
fun Link(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier.clickable { if (enabled) onClick() },
        color = if (enabled) MaterialTheme.colors.primary else LightGray
    )
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
