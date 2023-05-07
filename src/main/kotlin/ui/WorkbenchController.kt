package org.ossreviewtoolkit.workbench.ui

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

import java.nio.file.Path

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

import org.ossreviewtoolkit.utils.common.safeMkdirs
import org.ossreviewtoolkit.utils.ort.ortDataDirectory
import org.ossreviewtoolkit.workbench.model.OrtApiState
import org.ossreviewtoolkit.workbench.model.OrtModel
import org.ossreviewtoolkit.workbench.model.OrtModelInfo
import org.ossreviewtoolkit.workbench.model.WorkbenchSettings
import org.ossreviewtoolkit.workbench.state.DialogState

private const val ORT_WORKBENCH_CONFIG_DIRNAME = "workbench"
private const val ORT_WORKBENCH_CONFIG_FILENAME = "settings.yml"

class WorkbenchController {
    companion object {
        private val settingsMapper =
            YAMLMapper(YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)).apply {
                registerKotlinModule()
            }
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    private val mutex = Mutex()

    private val settingsFile =
        ortDataDirectory.resolve(ORT_WORKBENCH_CONFIG_DIRNAME).resolve(ORT_WORKBENCH_CONFIG_FILENAME)

    private val _settings = MutableStateFlow(WorkbenchSettings.default())
    val settings: StateFlow<WorkbenchSettings> = _settings

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _ortModels = MutableStateFlow(emptyList<OrtModel>())
    val ortModels: StateFlow<List<OrtModel>> = _ortModels

    private val _ortModel = MutableStateFlow<OrtModel?>(null)
    val ortModel: StateFlow<OrtModel?> = _ortModel

    val openResultDialog = DialogState<Path?>()

    init {
        scope.launch { loadSettings() }
    }

    suspend fun openOrtResult() {
        val newOrtModel = OrtModel(settings)

        if (newOrtModel.state.value !in listOf(OrtApiState.LOADING_RESULT, OrtApiState.PROCESSING_RESULT)) {
            val path = openResultDialog.awaitResult()
            if (path != null) {
                _ortModels.value = _ortModels.value + newOrtModel
                _ortModel.value = newOrtModel
                newOrtModel.loadOrtResult(path.toFile())
            }
        }
    }

    suspend fun updateSettings(settings: WorkbenchSettings) {
        mutex.withLock {
            withContext(Dispatchers.IO + NonCancellable) {
                _settings.value = settings
                saveSettings(settings)
            }
        }
    }

    private fun loadSettings() {
        val settings =
            settingsFile.takeIf { it.isFile }?.let { settingsMapper.readValue(it) } ?: WorkbenchSettings.default()

        _settings.value = settings

        if (!settingsFile.exists()) {
            saveSettings(settings)
        }
    }

    private fun saveSettings(settings: WorkbenchSettings) {
        runCatching {
            settingsFile.parentFile.safeMkdirs()
            settingsMapper.writeValue(settingsFile, settings)
        }.onFailure {
            _error.value = "Could not save settings at ${settingsFile.absolutePath}: ${it.message}"
        }
    }

    fun selectOrtModel(ortModelInfo: OrtModelInfo) {
        scope.launch {
            ortModels.value.find { it.info.value == ortModelInfo }?.let {
                _ortModel.value = it
            }
        }
    }

    fun closeOrtModel(ortModelInfo: OrtModelInfo) {
        scope.launch {
            ortModels.value.find { it.info.value == ortModelInfo }?.let {
                _ortModels.value = _ortModels.value - it
                if (ortModel.value?.info?.value == ortModelInfo) {
                    _ortModel.value = _ortModels.value.firstOrNull()
                }
            }
        }
    }
}
