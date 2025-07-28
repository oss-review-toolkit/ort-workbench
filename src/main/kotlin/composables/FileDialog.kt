package org.ossreviewtoolkit.workbench.composables

import androidx.compose.runtime.Composable

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker

import java.nio.file.Path

import kotlinx.coroutines.runBlocking

@Composable
fun FileDialog(
    title: String,
    isLoad: Boolean,
    fileExtensionFilter: List<String> = emptyList(),
    onResult: (result: Path?) -> Unit
) {
    require(isLoad)

    val fileType = FileKitType.File(fileExtensionFilter)
    val pickedFile = runBlocking { FileKit.openFilePicker(fileType, FileKitMode.Single, title) }

    pickedFile?.run { onResult(file.toPath()) }
}
