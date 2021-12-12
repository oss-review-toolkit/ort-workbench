package org.ossreviewtoolkit.workbench.util

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CaptionedColumn(caption: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = modifier) {
        Text(
            text = caption,
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.primary
        )
        content()
    }
}
