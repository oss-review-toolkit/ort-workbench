package org.ossreviewtoolkit.workbench.ui

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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import java.nio.file.Path

import kotlinx.coroutines.launch

import org.ossreviewtoolkit.workbench.state.AppState
import org.ossreviewtoolkit.workbench.state.DialogState
import org.ossreviewtoolkit.workbench.state.FileType
import org.ossreviewtoolkit.workbench.state.OrtConfigFileInfo
import org.ossreviewtoolkit.workbench.state.SettingsState
import org.ossreviewtoolkit.workbench.theme.Error
import org.ossreviewtoolkit.workbench.util.BrowseDirectoryLink
import org.ossreviewtoolkit.workbench.util.DirectoryChooser
import org.ossreviewtoolkit.workbench.util.EditFileLink
import org.ossreviewtoolkit.workbench.util.Preview
import org.ossreviewtoolkit.workbench.util.WebLink

@Composable
fun Settings(appState: AppState) {
    val state = appState.settings

    if (state.settings == null) {
        LaunchedEffect(state.ortConfigDir) { state.loadSettings() }
    }

    val scrollState = rememberScrollState()

    Box {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp).verticalScroll(scrollState)) {
            ConfigSettings(state)
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState = scrollState)
        )
    }
}

@Composable
fun ConfigSettings(state: SettingsState) {
    Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
        Text("Configuration files", style = MaterialTheme.typography.h6)

        val configDirState = when (state.ortConfigDir.fileInfo.exists) {
            true -> OptionState.SUCCESS
            else -> OptionState.ERROR
        }

        val scope = rememberCoroutineScope()
        val selectDirectoryDialogState = remember { DialogState<Path?>() }

        OptionCard(state.ortConfigDir, state = configDirState) {
            Button(onClick = {
                if (!selectDirectoryDialogState.isAwaiting) {
                    scope.launch {
                        val path = selectDirectoryDialogState.awaitResult()
                        if (path != null) {
                            state.setConfigDir(path)
                        }
                    }
                }
            }) {
                Text("Select")
            }
        }

        if (selectDirectoryDialogState.isAwaiting) {
            DirectoryChooser(currentDirectory = state.ortConfigDir.fileInfo.file) {
                selectDirectoryDialogState.onResult(it?.toPath())
            }
        }

        state.ortConfigFiles.forEach { config ->
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
            elevation = 0.dp
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
private fun ConfigSettingsPreview() {
    Preview {
        ConfigSettings(state = SettingsState())
    }
}
