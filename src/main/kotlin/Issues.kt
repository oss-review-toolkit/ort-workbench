package org.ossreviewtoolkit.workbench

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
@Preview
fun Issues() {
    Column(
        modifier = Modifier.padding(15.dp)
    ) {
        Text("Issues", style = MaterialTheme.typography.h3)
    }
}
