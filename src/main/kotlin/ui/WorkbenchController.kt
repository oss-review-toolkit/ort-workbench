package org.ossreviewtoolkit.workbench.ui

import java.nio.file.Path

import org.ossreviewtoolkit.workbench.model.OrtApiState
import org.ossreviewtoolkit.workbench.model.OrtModel
import org.ossreviewtoolkit.workbench.state.DialogState

class WorkbenchController {
    val ortModel = OrtModel()

    val openResultDialog = DialogState<Path?>()

    suspend fun openOrtResult() {
        if (ortModel.state.value !in listOf(OrtApiState.LOADING_RESULT, OrtApiState.PROCESSING_RESULT)) {
            val path = openResultDialog.awaitResult()
            if (path != null) {
                ortModel.loadOrtResult(path.toFile())
            }
        }
    }
}
