package org.ossreviewtoolkit.workbench.ui

import java.nio.file.Path

import org.ossreviewtoolkit.workbench.model.OrtApiState
import org.ossreviewtoolkit.workbench.model.OrtModel
import org.ossreviewtoolkit.workbench.state.DialogState
import org.ossreviewtoolkit.workbench.ui.dependencies.DependenciesViewModel
import org.ossreviewtoolkit.workbench.ui.issues.IssuesViewModel
import org.ossreviewtoolkit.workbench.ui.packages.PackagesViewModel
import org.ossreviewtoolkit.workbench.ui.settings.SettingsViewModel
import org.ossreviewtoolkit.workbench.ui.summary.SummaryViewModel
import org.ossreviewtoolkit.workbench.ui.violations.ViolationsViewModel
import org.ossreviewtoolkit.workbench.ui.vulnerabilities.VulnerabilitiesViewModel

class WorkbenchController(val ortModel: OrtModel = OrtModel.INSTANCE) {
    val dependenciesViewModel = DependenciesViewModel(ortModel)
    val issuesViewModel = IssuesViewModel(ortModel)
    val packagesViewModel = PackagesViewModel(ortModel)
    val settingsViewModel = SettingsViewModel(ortModel)
    val summaryViewModel = SummaryViewModel(ortModel)
    val violationsViewModel = ViolationsViewModel(ortModel)
    val vulnerabilitiesViewModel = VulnerabilitiesViewModel(ortModel)

    val openResultDialog = DialogState<Path?>()

    suspend fun openOrtResult() {
        if (ortModel.state.value !in listOf(OrtApiState.LOADING_RESULT, OrtApiState.PROCESSING_RESULT)) {
            val path = openResultDialog.awaitResult()
            if (path != null) {
                OrtModel.INSTANCE.loadOrtResult(path.toFile())
            }
        }
    }
}
