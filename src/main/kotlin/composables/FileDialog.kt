package org.ossreviewtoolkit.workbench.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker

import java.nio.file.Path

@Composable
fun FileDialog(
    title: String,
    isLoad: Boolean,
    fileExtensionFilter: List<String> = emptyList(),
    onResult: (result: Path?) -> Unit
) {
    require(isLoad)

    val fileType = FileKitType.File(fileExtensionFilter)

    LaunchedEffect(Unit) {
        val pickedFile = FileKit.openFilePicker(
            fileType,
            FileKitMode.Single,
            directory = null,
            FileKitDialogSettings(title)
        )

        onResult(pickedFile?.file?.toPath())
    }
}
