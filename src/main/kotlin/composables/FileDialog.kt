package org.ossreviewtoolkit.workbench.composables

import androidx.compose.runtime.Composable

import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType

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

    val fileType = PickerType.File(fileExtensionFilter)
    val pickedFile = runBlocking { FileKit.pickFile(fileType, PickerMode.Single, title) }

    pickedFile?.run { onResult(file.toPath()) }
}
