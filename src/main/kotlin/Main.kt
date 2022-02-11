package org.ossreviewtoolkit.workbench

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication

import org.ossreviewtoolkit.workbench.ui.App
import org.ossreviewtoolkit.workbench.ui.rememberAppState

fun main() = singleWindowApplication(
    title = "ORT Workbench",
    state = WindowState(
        size = DpSize(1440.dp, 810.dp)
    ),
    icon = BitmapPainter(useResource("app-icon/icon.png", ::loadImageBitmap))
) {
    App(rememberAppState())
}
