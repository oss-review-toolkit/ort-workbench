package org.ossreviewtoolkit.workbench

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication

import java.net.URI

import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap

import org.ossreviewtoolkit.workbench.ort_workbench.generated.resources.Res
import org.ossreviewtoolkit.workbench.ui.App
import org.ossreviewtoolkit.workbench.ui.WorkbenchController

@OptIn(ExperimentalResourceApi::class)
fun main() {
    val workbenchController = WorkbenchController()

    // See https://github.com/JetBrains/compose-multiplatform/issues/2369.
    val iconBytes = URI.create(Res.getUri("drawable/app-icon/icon.png")).toURL().readBytes()
    val icon = BitmapPainter(iconBytes.decodeToImageBitmap())

    singleWindowApplication(
        title = "ORT Workbench",
        state = WindowState(
            size = DpSize(1440.dp, 810.dp)
        ),
        icon = icon
    ) {
        App(workbenchController)
    }
}
