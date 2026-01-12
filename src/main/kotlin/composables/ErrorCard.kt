package org.ossreviewtoolkit.workbench.composables

import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ErrorCard(message: String) {
    Card(backgroundColor = MaterialTheme.colors.error) {
        Text(text = message)
    }
}

@Composable
@Preview
fun ErrorCardPreview() {
    Preview {
        ErrorCard("Some error message.")
    }
}
