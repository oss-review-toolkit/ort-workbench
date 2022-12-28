package org.ossreviewtoolkit.workbench.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SidePanel(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(visible = visible) {
        Surface(
            modifier = Modifier.width(500.dp).fillMaxHeight(),
            color = MaterialTheme.colors.surface,
            elevation = 8.dp
        ) {
            content()
        }
    }
}
