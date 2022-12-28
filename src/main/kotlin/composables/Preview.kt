package org.ossreviewtoolkit.workbench.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import org.ossreviewtoolkit.workbench.model.WorkbenchTheme
import org.ossreviewtoolkit.workbench.theme.OrtWorkbenchTheme

@Composable
fun Preview(content: @Composable () -> Unit) {
    OrtWorkbenchTheme(WorkbenchTheme.AUTO) {
        Box(modifier = Modifier.padding(10.dp)) {
            content()
        }
    }
}
