package org.ossreviewtoolkit.workbench.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.utils.common.titlecase
import org.ossreviewtoolkit.workbench.composables.FileDialog
import org.ossreviewtoolkit.workbench.model.OrtApiState
import org.ossreviewtoolkit.workbench.navigation.Navigation
import org.ossreviewtoolkit.workbench.navigation.Screen
import org.ossreviewtoolkit.workbench.navigation.rememberNavigationController
import org.ossreviewtoolkit.workbench.theme.OrtWorkbenchTheme
import org.ossreviewtoolkit.workbench.ui.dependencies.Dependencies
import org.ossreviewtoolkit.workbench.ui.issues.Issues
import org.ossreviewtoolkit.workbench.ui.packagedetails.PackageDetails
import org.ossreviewtoolkit.workbench.ui.packages.Packages
import org.ossreviewtoolkit.workbench.ui.settings.Settings
import org.ossreviewtoolkit.workbench.ui.summary.Summary
import org.ossreviewtoolkit.workbench.ui.violations.Violations
import org.ossreviewtoolkit.workbench.ui.vulnerabilities.Vulnerabilities
import org.ossreviewtoolkit.workbench.utils.MaterialIcon

@Composable
fun App(state: AppState) {
    val apiState by state.ortModel.state.collectAsState()
    val settings by state.ortModel.settings.collectAsState()
    val scope = rememberCoroutineScope()

    fun loadResult() = scope.launch { state.openOrtResult() }

    OrtWorkbenchTheme(settings.theme) {
        Surface(color = MaterialTheme.colors.background) {
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

sealed class MainScreen(val name: String, val icon: MaterialIcon) : Screen {
    object Dependencies : MainScreen("Dependencies", MaterialIcon.ACCOUNT_TREE)
    object Issues : MainScreen("Issues", MaterialIcon.BUG_REPORT)
    class PackageDetails(val pkg: Identifier) : MainScreen("Package Details", MaterialIcon.INVENTORY)
    object Packages : MainScreen("Packages", MaterialIcon.INVENTORY)
    object Settings : MainScreen("Settings", MaterialIcon.SETTINGS)
    object Summary : MainScreen("Summary", MaterialIcon.ASSESSMENT)
    object RuleViolations : MainScreen("Rule Violations", MaterialIcon.GAVEL)
    object Vulnerabilities : MainScreen("Vulnerabilities", MaterialIcon.LOCK_OPEN)
}

@Composable
fun MainLayout(state: AppState, apiState: OrtApiState, onLoadResult: () -> Unit) {
    val navController = rememberNavigationController<MainScreen>(MainScreen.Summary)

    Navigation(navController) { currentScreen ->
        (currentScreen ?: MainScreen.Summary).let { screen ->
            Column {
                TopBar()

                Row {
                    Menu(screen, apiState, onSwitchScreen = { navController.replace(it) })
                    Content(
                        screen,
                        state,
                        onLoadResult,
                        onSwitchScreen = { navController.replace(it) },
                        onPushScreen = { navController.push(it) },
                        onBack = { navController.pop() }
                    )
                }
            }
        }
    }
}

@Composable
private fun Content(
    currentScreen: MainScreen,
    state: AppState,
    onLoadResult: () -> Unit,
    onSwitchScreen: (MainScreen) -> Unit,
    onPushScreen: (MainScreen) -> Unit,
    onBack: () -> Unit
) {
    SetupMaterialRichText {
        when (currentScreen) {
            is MainScreen.Summary -> Summary(state.summaryViewModel, onSwitchScreen, onLoadResult)
            is MainScreen.Packages -> Packages(state.packagesViewModel, onPushScreen)
            is MainScreen.PackageDetails -> PackageDetails(currentScreen.pkg, onBack)
            is MainScreen.Dependencies -> Dependencies(state.dependenciesViewModel)
            is MainScreen.Issues -> Issues(state.issuesViewModel)
            is MainScreen.RuleViolations -> Violations(state.violationsViewModel)
            is MainScreen.Vulnerabilities -> Vulnerabilities(state.vulnerabilitiesViewModel)
            is MainScreen.Settings -> Settings(state.settingsViewModel)
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
        val image = if (MaterialTheme.colors.isLight) "ort-black.png" else "ort-white.png"

        Image(painter = painterResource(image), contentDescription = "OSS Review Toolkit")

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
