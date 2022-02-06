package org.ossreviewtoolkit.workbench.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.AwtWindow

import java.awt.FileDialog
import java.io.File
import java.io.FilenameFilter
import java.nio.file.Path

@Composable
fun FileDialog(
    title: String,
    isLoad: Boolean,
    fileExtensionFilter: List<String> = emptyList(),
    onResult: (result: Path?) -> Unit
) = AwtWindow(
    create = {
        object : FileDialog(ComposeWindow(), "Choose a file", if (isLoad) LOAD else SAVE) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    if (file != null) {
                        onResult(File(directory).resolve(file).toPath())
                    } else {
                        onResult(null)
                    }
                }
            }
        }.apply {
            this.title = title

            if (fileExtensionFilter.isNotEmpty()) {
                file = fileExtensionFilter.joinToString(";") { "*.$it" }
                filenameFilter = FilenameFilter { _, name -> name.substringAfterLast(".") in fileExtensionFilter }
            }
        }
    },
    dispose = FileDialog::dispose
)
