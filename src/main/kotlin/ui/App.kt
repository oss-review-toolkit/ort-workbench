@file:OptIn(ExperimentalComposeUiApi::class)

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.DragData
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.onExternalDrag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

import java.io.File
import java.net.URI

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.utils.common.enumSetOf
import org.ossreviewtoolkit.utils.common.titlecase
import org.ossreviewtoolkit.workbench.composables.FileDialog
import org.ossreviewtoolkit.workbench.model.OrtApiState
import org.ossreviewtoolkit.workbench.model.OrtModel
import org.ossreviewtoolkit.workbench.navigation.BackstackEntry
import org.ossreviewtoolkit.workbench.navigation.NavController
import org.ossreviewtoolkit.workbench.navigation.NavHost
import org.ossreviewtoolkit.workbench.navigation.viewModel
import org.ossreviewtoolkit.workbench.theme.OrtWorkbenchTheme
import org.ossreviewtoolkit.workbench.ui.dependencies.Dependencies
import org.ossreviewtoolkit.workbench.ui.issues.Issues
import org.ossreviewtoolkit.workbench.ui.packagedetails.PackageDetails
import org.ossreviewtoolkit.workbench.ui.packagedetails.PackageDetailsViewModel
import org.ossreviewtoolkit.workbench.ui.packages.Packages
import org.ossreviewtoolkit.workbench.ui.settings.Settings
import org.ossreviewtoolkit.workbench.ui.summary.Summary
import org.ossreviewtoolkit.workbench.ui.violations.Violations
import org.ossreviewtoolkit.workbench.ui.vulnerabilities.Vulnerabilities

@Composable
fun App(controller: WorkbenchController) {
    val settings by controller.settings.collectAsState()
    val ortModel by controller.ortModel.collectAsState()
    val apiState = ortModel?.state?.collectAsState()?.value

    val scope = rememberCoroutineScope()

    fun loadResult() = scope.launch {
        val isNotLoadingFile = controller.ortModels.value.none {
            it.state.value in enumSetOf(OrtApiState.LOADING_RESULT, OrtApiState.PROCESSING_RESULT)
        }

        if (isNotLoadingFile) {
            val path = controller.openResultDialog.awaitResult()
            if (path != null) controller.openOrtResult(path.toFile())
        }
    }

    OrtWorkbenchTheme(settings.theme) {
        var isDragging by remember { mutableStateOf(false) }

        Surface(
            modifier = Modifier.onExternalDrag(
                onDragStart = { isDragging = true },
                onDragExit = { isDragging = false },
                onDrop = { state ->
                    val data = state.dragData
                    if (data is DragData.FilesList) {
                        val files = data.readFiles().map { File(URI(it)) }
                        scope.launch { files.forEach { file -> controller.openOrtResult(file) } }
                    }
                    isDragging = false
                }
            ),
            color = when {
                isDragging -> MaterialTheme.colors.primaryVariant
                else -> MaterialTheme.colors.background
            }
        ) {
            if (apiState == OrtApiState.READY) {
                MainLayout(controller, ::loadResult)
            } else {
                LoadResult(controller, ::loadResult)
            }

            if (controller.openResultDialog.isAwaiting) {
                FileDialog(
                    title = "Load ORT result",
                    isLoad = true,
                    fileExtensionFilter = listOf("json", "yml"),
                    onResult = { controller.openResultDialog.onResult(it) }
                )
            }
        }
    }
}

private fun selectMenuItem(
    controller: WorkbenchController,
    ortModel: OrtModel,
    navController: NavController,
    item: MenuItem
) {
    val screen = when (item) {
        MenuItem.DEPENDENCIES -> MainScreen.Dependencies(ortModel)
        MenuItem.ISSUES -> MainScreen.Issues(ortModel)
        MenuItem.PACKAGES -> MainScreen.Packages(ortModel)
        MenuItem.RULE_VIOLATIONS -> MainScreen.RuleViolations(ortModel)
        MenuItem.SETTINGS -> MainScreen.Settings(controller)
        MenuItem.SUMMARY -> MainScreen.Summary(ortModel)
        MenuItem.VULNERABILITIES -> MainScreen.Vulnerabilities(ortModel)
    }

    navController.navigate(screen, launchSingleTop = true)
}

@Composable
fun MainLayout(controller: WorkbenchController, onLoadResult: () -> Unit) {
    val ortModelState = controller.ortModel.collectAsState()
    val ortModel = ortModelState.value ?: return
    val apiState by ortModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    val navController = ortModel.navController

    NavHost(navController) { backstackEntry ->
        Column {
            val ortModels by controller.ortModels.collectAsState()
            val ortModelInfos by combine(ortModels.map { it.info }) { it.filterNotNull() }.collectAsState(emptyList())
            val selectedOrtModelInfo by ortModel.info.collectAsState()
            val useOnlyResolvedConfiguration by ortModel.useOnlyResolvedConfiguration.collectAsState()

            TopBar(
                selectedOrtModelInfo,
                ortModelInfos,
                onLoadFile = onLoadResult,
                onSelectModel = { controller.selectOrtModel(it) },
                onCloseModel = { controller.closeOrtModel(it) }
            )

            Row {
                val currentMenuItem = (backstackEntry.screen as? MainScreen)?.menuItem

                Menu(
                    currentMenuItem,
                    apiState,
                    useOnlyResolvedConfiguration = useOnlyResolvedConfiguration,
                    onSwitchUseOnlyResolvedConfiguration = {
                        scope.launch { ortModel.switchUseOnlyResolvedConfiguration() }
                    },
                    onSelectMenuItem = { selectMenuItem(controller, ortModel, navController, it) }
                )

                Content(
                    backstackEntry,
                    onSelectPackage = { pkgId ->
                        navController.navigate(
                            MainScreen.PackageDetails(ortModel, pkgId),
                            launchSingleTop = false
                        )
                    },
                    onBack = { navController.back() }
                )
            }
        }
    }
}

@Composable
private fun Content(
    backstackEntry: BackstackEntry,
    onSelectPackage: (Identifier) -> Unit,
    onBack: () -> Unit
) {
    if (backstackEntry.screen !is MainScreen<*>) {
        // TODO: Show error.
        return
    }

    when (backstackEntry.screen) {
        is MainScreen.Summary -> Summary(backstackEntry.viewModel())
        is MainScreen.Packages -> Packages(backstackEntry.viewModel(), onSelectPackage)
        is MainScreen.Dependencies -> Dependencies(backstackEntry.viewModel())
        is MainScreen.Issues -> Issues(backstackEntry.viewModel())
        is MainScreen.RuleViolations -> Violations(backstackEntry.viewModel())
        is MainScreen.Vulnerabilities -> Vulnerabilities(backstackEntry.viewModel())
        is MainScreen.Settings -> Settings(backstackEntry.viewModel())

        is MainScreen.PackageDetails -> {
            val viewModel = backstackEntry.viewModel<PackageDetailsViewModel>()
            val state by viewModel.model.collectAsState()
            PackageDetails(state, onBack)
        }
    }
}

@Composable
private fun LoadResult(controller: WorkbenchController, onLoadResult: () -> Unit) {
    val ortModelState = controller.ortModel.collectAsState()
    val ortModel = ortModelState.value
    val apiState = ortModel?.state?.collectAsState()?.value
    val error by controller.error.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(25.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val image = if (MaterialTheme.colors.isLight) "ort-black.png" else "ort-white.png"

        Image(painter = painterResource(image), contentDescription = "OSS Review Toolkit")

        if (apiState in listOf(OrtApiState.LOADING_RESULT, OrtApiState.PROCESSING_RESULT)) {
            CircularProgressIndicator()
            Text("${apiState?.name.orEmpty().replace("_", " ").titlecase()}...")
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
