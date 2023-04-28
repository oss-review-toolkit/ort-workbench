package org.ossreviewtoolkit.workbench.ui.settings

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import java.nio.file.Path

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

import org.ossreviewtoolkit.workbench.composables.BrowseDirectoryLink
import org.ossreviewtoolkit.workbench.composables.DirectoryChooser
import org.ossreviewtoolkit.workbench.composables.EditFileLink
import org.ossreviewtoolkit.workbench.composables.Preview
import org.ossreviewtoolkit.workbench.composables.WebLink
import org.ossreviewtoolkit.workbench.model.OrtModel
import org.ossreviewtoolkit.workbench.model.WorkbenchTheme
import org.ossreviewtoolkit.workbench.state.DialogState
import org.ossreviewtoolkit.workbench.theme.Error

@Composable
fun Settings(viewModel: SettingsViewModel) {
    val tab by viewModel.tab.collectAsState()

    Column {
        val scrollState = rememberScrollState()

        TabRow(selectedTabIndex = tab.ordinal, backgroundColor = MaterialTheme.colors.primary) {
            SettingsTab.values().forEach {
                Tab(
                    text = { Text(it.title, style = MaterialTheme.typography.h5) },
                    selected = it == tab,
                    onClick = {
                        viewModel.setTab(it)
                        runBlocking { scrollState.scrollTo(0) }
                    }
                )
            }
        }

        Box {
            Column(modifier = Modifier.padding(horizontal = 20.dp).verticalScroll(scrollState)) {
                when (tab) {
                    SettingsTab.CONFIG_FILES -> ConfigFilesSettings(viewModel)
                    SettingsTab.WORKBENCH -> WorkbenchSettings(viewModel)
                }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState = scrollState)
            )
        }
    }
}

@Composable
fun ConfigFilesSettings(viewModel: SettingsViewModel) {
    val ortConfigDir by viewModel.ortConfigDir.collectAsState()
    val ortConfigFiles by viewModel.ortConfigFiles.collectAsState()

    Column(modifier = Modifier.padding(vertical = 15.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        val configDirState = when (ortConfigDir.fileInfo.exists) {
            true -> OptionState.SUCCESS
            else -> OptionState.ERROR
        }

        val scope = rememberCoroutineScope()
        val selectDirectoryDialogState = remember { DialogState<Path?>() }

        OptionCard(ortConfigDir, state = configDirState) {
            Button(onClick = {
                if (!selectDirectoryDialogState.isAwaiting) {
                    scope.launch {
                        val path = selectDirectoryDialogState.awaitResult()
                        if (path != null) {
                            viewModel.setConfigDir(path)
                        }
                    }
                }
            }) {
                Text("Select")
            }
        }

        if (selectDirectoryDialogState.isAwaiting) {
            DirectoryChooser(currentDirectory = ortConfigDir.fileInfo.file) {
                selectDirectoryDialogState.onResult(it?.toPath())
            }
        }

        ortConfigFiles.forEach { config ->
            OptionCard(
                config,
                state = when {
                    configDirState != OptionState.SUCCESS -> OptionState.DISABLED
                    config.fileInfo.exists -> OptionState.SUCCESS
                    else -> OptionState.ERROR
                }
            )
        }
    }
}

enum class OptionState {
    ERROR, DISABLED, SUCCESS
}

@Composable
fun OptionCard(
    config: OrtConfigFileInfo,
    state: OptionState,
    selector: @Composable ColumnScope.() -> Unit = {}
) {
    val alpha = if (state == OptionState.DISABLED) LocalContentAlpha.current / 2f else LocalContentAlpha.current

    CompositionLocalProvider(LocalContentAlpha provides alpha) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            config.configFile.name,
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold
                        )
                        Text(config.configFile.description, style = MaterialTheme.typography.subtitle2)

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (config.fileInfo.exists) {
                                when (config.fileInfo.type) {
                                    FileType.FILE -> EditFileLink("Open in editor", config.fileInfo.file)
                                    FileType.DIRECTORY -> BrowseDirectoryLink("Browse directory", config.fileInfo.file)
                                }
                            }

                            WebLink("Documentation", config.configFile.documentationUrl)

                            if (config.configFile.exampleUrl != null) {
                                WebLink("Show example", config.configFile.exampleUrl)
                            }

                            // TODO: Add option to import the example file into the local config directory.
                        }
                    }

                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            config.fileInfo.file.path,
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Bold
                        )

                        config.fileInfo.errorMessage?.let { error ->
                            Text(
                                error,
                                style = MaterialTheme.typography.subtitle2,
                                fontWeight = FontWeight.Bold,
                                color = Error
                            )
                        }
                    }

                    Column {
                        selector()
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun ConfigFilesSettingsPreview() {
    Preview {
        ConfigFilesSettings(SettingsViewModel(OrtModel()))
    }
}

@Composable
private fun WorkbenchSettings(viewModel: SettingsViewModel) {
    val scope = rememberCoroutineScope()
    val theme by viewModel.theme.collectAsState()

    Column(modifier = Modifier.padding(vertical = 15.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                Text("Theme", style = MaterialTheme.typography.h5)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Use system theme", modifier = Modifier.weight(1f))

                    Switch(
                        checked = theme == WorkbenchTheme.AUTO,
                        onCheckedChange = { enabled ->
                            scope.launch {
                                viewModel.setTheme(if (enabled) WorkbenchTheme.AUTO else WorkbenchTheme.LIGHT)
                            }
                        }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Use dark theme", modifier = Modifier.weight(1f))

                    Switch(
                        enabled = theme != WorkbenchTheme.AUTO,
                        checked = theme == WorkbenchTheme.DARK,
                        onCheckedChange = { enabled ->
                            scope.launch {
                                viewModel.setTheme(if (enabled) WorkbenchTheme.DARK else WorkbenchTheme.LIGHT)
                            }
                        }
                    )
                }
            }
        }
    }
}
