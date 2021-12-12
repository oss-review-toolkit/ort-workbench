package org.ossreviewtoolkit.workbench.util

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable

@Composable
fun Link(text: String, onClick: () -> Unit) {
    TextButton(onClick) {
        Text(text)
    }
}

@Composable
@Preview
private fun LinkPreview() {
    Preview {
        Link("Text link") {}
    }
}
