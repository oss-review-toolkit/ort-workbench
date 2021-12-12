package org.ossreviewtoolkit.workbench

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import org.ossreviewtoolkit.workbench.state.rememberAppState

fun main() = singleWindowApplication(
    title = "ORT Workbench",
    state = WindowState(
        size = DpSize(1440.dp, 810.dp)
    )
) {
    App(rememberAppState())
}
