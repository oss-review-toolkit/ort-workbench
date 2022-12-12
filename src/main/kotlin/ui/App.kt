package org.ossreviewtoolkit.workbench.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

import com.halilibo.richtext.ui.material.SetupMaterialRichText

import kotlinx.coroutines.launch

import org.ossreviewtoolkit.utils.common.titlecase
import org.ossreviewtoolkit.workbench.model.OrtApiState
import org.ossreviewtoolkit.workbench.theme.OrtWorkbenchTheme
import org.ossreviewtoolkit.workbench.theme.VeryLightGray
import org.ossreviewtoolkit.workbench.ui.dependencies.Dependencies
import org.ossreviewtoolkit.workbench.ui.issues.Issues
import org.ossreviewtoolkit.workbench.ui.packages.Packages
import org.ossreviewtoolkit.workbench.ui.settings.Settings
import org.ossreviewtoolkit.workbench.ui.summary.Summary
import org.ossreviewtoolkit.workbench.ui.violations.Violations
import org.ossreviewtoolkit.workbench.ui.vulnerabilities.Vulnerabilities
import org.ossreviewtoolkit.workbench.util.FileDialog

@Composable
fun App(state: AppState) {
    val apiState by state.ortModel.state.collectAsState()
    val settings by state.ortModel.settings.collectAsState()
    val scope = rememberCoroutineScope()

    fun loadResult() = scope.launch { state.openOrtResult() }

    OrtWorkbenchTheme(settings.theme) {
        Surface {
            if (apiState == OrtApiState.READY) {
                MainLayout(state, apiState, ::loadResult)
            } else {
                LoadResult(state, apiState, ::loadResult)
            }

            if (state.openResultDialog.isAwaiting) {
                FileDialog(
                    title = "Load ORT result",
                    isLoad = true,
                    fileExtensionFilter = listOf("json", "yml"),
                    onResult = { state.openResultDialog.onResult(it) }
                )
            }
        }
    }
}

@Composable
fun MainLayout(state: AppState, apiState: OrtApiState, onLoadResult: () -> Unit) {
    Column {
        TopBar()

        Row {
            Menu(state.currentScreen, apiState, state::switchScreen)
            Content(state, onLoadResult)
        }
    }
}

@Composable
private fun Content(state: AppState, onLoadResult: () -> Unit) {
    Surface(modifier = Modifier.fillMaxHeight(), color = VeryLightGray) {
        SetupMaterialRichText {
            when (state.currentScreen) {
                MenuItem.SUMMARY -> Summary(state.summaryViewModel, state::switchScreen, onLoadResult)
                MenuItem.PACKAGES -> Packages(state.packagesViewModel)
                MenuItem.DEPENDENCIES -> Dependencies(state.dependenciesViewModel)
                MenuItem.ISSUES -> Issues(state.issuesViewModel)
                MenuItem.RULE_VIOLATIONS -> Violations(state.violationsViewModel)
                MenuItem.VULNERABILITIES -> Vulnerabilities(state.vulnerabilitiesViewModel)
                MenuItem.SETTINGS -> Settings(state.settingsViewModel)
            }
        }
    }
}

@Composable
private fun LoadResult(state: AppState, apiState: OrtApiState, onLoadResult: () -> Unit) {
    val error by state.ortModel.error.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(25.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource("ort-black.png"), contentDescription = "OSS Review Toolkit")

        if (apiState in listOf(OrtApiState.LOADING_RESULT, OrtApiState.PROCESSING_RESULT)) {
            CircularProgressIndicator()
            Text("${apiState.name.replace("_", " ").titlecase()}...")
        } else {
            Button(onClick = onLoadResult) {
                Text("Load ORT Result")
            }
        }

        error?.let {
            Card(backgroundColor = MaterialTheme.colors.error) {
                Text(text = it)
            }
        }
    }
}
