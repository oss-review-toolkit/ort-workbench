package org.ossreviewtoolkit.workbench.composables

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CaptionedText(caption: String, text: String, modifier: Modifier = Modifier) {
    CaptionedColumn(caption, modifier = modifier) { Text(text) }
}
