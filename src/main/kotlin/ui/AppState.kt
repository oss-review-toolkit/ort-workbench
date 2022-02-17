package org.ossreviewtoolkit.workbench.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import java.nio.file.Path

import org.ossreviewtoolkit.workbench.model.OrtModel
import org.ossreviewtoolkit.workbench.state.DialogState
import org.ossreviewtoolkit.workbench.ui.dependencies.DependenciesViewModel
import org.ossreviewtoolkit.workbench.ui.issues.IssuesViewModel
import org.ossreviewtoolkit.workbench.ui.packages.PackagesViewModel
import org.ossreviewtoolkit.workbench.ui.settings.SettingsViewModel
import org.ossreviewtoolkit.workbench.ui.summary.SummaryViewModel
import org.ossreviewtoolkit.workbench.ui.violations.ViolationsViewModel
import org.ossreviewtoolkit.workbench.ui.vulnerabilities.VulnerabilitiesViewModel

@Composable
fun rememberAppState() = remember { AppState() }

class AppState(val ortModel: OrtModel = OrtModel.INSTANCE) {
    var currentScreen by mutableStateOf(MenuItem.SUMMARY)
        private set

    val dependenciesViewModel = DependenciesViewModel(ortModel)
    val issuesViewModel = IssuesViewModel(ortModel)
    val packagesViewModel = PackagesViewModel(ortModel)
    val settingsViewModel = SettingsViewModel(ortModel)
    val summaryViewModel = SummaryViewModel(ortModel)
    val violationsViewModel = ViolationsViewModel(ortModel)
    val vulnerabilitiesViewModel = VulnerabilitiesViewModel(ortModel)

    val openResultDialog = DialogState<Path?>()

    fun switchScreen(menuItem: MenuItem) {
        currentScreen = menuItem
    }

    suspend fun openOrtResult() {
        val path = openResultDialog.awaitResult()
        if (path != null) {
            OrtModel.INSTANCE.loadOrtResult(path.toFile())
        }
    }
}
