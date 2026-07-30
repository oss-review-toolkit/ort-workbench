package org.ossreviewtoolkit.workbench.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker

import java.io.File

@Composable
fun DirectoryChooser(currentDirectory: File? = null, onResult: (result: File?) -> Unit) {
    LaunchedEffect(Unit) {
        val selectedDirectory = FileKit.openDirectoryPicker(
            directory = currentDirectory?.let { PlatformFile(it) }
        )

        onResult(selectedDirectory?.file)
    }
}
