package org.ossreviewtoolkit.workbench.model

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

import java.io.File

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

import org.ossreviewtoolkit.model.OrtResult
import org.ossreviewtoolkit.model.config.CopyrightGarbage
import org.ossreviewtoolkit.model.config.LicenseFilenamePatterns
import org.ossreviewtoolkit.model.config.OrtConfiguration
import org.ossreviewtoolkit.model.config.createFileArchiver
import org.ossreviewtoolkit.model.licenses.DefaultLicenseInfoProvider
import org.ossreviewtoolkit.model.licenses.LicenseInfoProvider
import org.ossreviewtoolkit.model.licenses.LicenseInfoResolver
import org.ossreviewtoolkit.model.readValue
import org.ossreviewtoolkit.model.utils.DefaultResolutionProvider
import org.ossreviewtoolkit.model.utils.FileArchiver
import org.ossreviewtoolkit.model.utils.PackageConfigurationProvider
import org.ossreviewtoolkit.model.utils.ResolutionProvider
import org.ossreviewtoolkit.model.utils.SimplePackageConfigurationProvider
import org.ossreviewtoolkit.model.utils.createLicenseInfoResolver
import org.ossreviewtoolkit.utils.core.ORT_CONFIG_FILENAME
import org.ossreviewtoolkit.utils.core.ORT_COPYRIGHT_GARBAGE_FILENAME
import org.ossreviewtoolkit.utils.core.ORT_PACKAGE_CONFIGURATIONS_DIRNAME
import org.ossreviewtoolkit.utils.core.ORT_RESOLUTIONS_FILENAME
import org.ossreviewtoolkit.utils.core.ortConfigDirectory
import org.ossreviewtoolkit.utils.core.ortDataDirectory

private const val ORT_WORKBENCH_CONFIG_DIRNAME = "workbench"
private const val ORT_WORKBENCH_CONFIG_FILENAME = "settings.yml"

class OrtModel {
    companion object {
        /**
         * The global instance of the [OrtModel], should be replaced with dependency injection later on.
         */
        val INSTANCE = OrtModel()

        private val settingsMapper =
            YAMLMapper(YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)).apply {
                registerKotlinModule()
            }
    }

    private val mutex = Mutex()

    private val settingsFile =
        ortDataDirectory.resolve(ORT_WORKBENCH_CONFIG_DIRNAME).resolve(ORT_WORKBENCH_CONFIG_FILENAME)

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _settings = MutableStateFlow(WorkbenchSettings.default())
    val settings: StateFlow<WorkbenchSettings> get() = _settings

    private val _state = MutableStateFlow(OrtApiState.UNINITIALIZED)
    val state: StateFlow<OrtApiState> get() = _state

    private val _ortResultFile: MutableStateFlow<File?> = MutableStateFlow(null)
    val ortResultFile: StateFlow<File?> get() = _ortResultFile

    private val _ortResult = MutableStateFlow<OrtResult?>(null)

    private val _api = MutableStateFlow(OrtApi.EMPTY)
    val api: StateFlow<OrtApi> get() = _api

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    init {
        scope.launch { loadSettings() }

        scope.launch {
            combine(
                settings.map { it.ortConfigDir },
                _ortResult
            ) { configDir, ortResult ->
                if (ortResult != null) {
                    _state.value = OrtApiState.PROCESSING_RESULT
                    createOrtApi(configDir, ortResult)
                } else {
                    null
                }
            }.collect { api ->
                if (api == null) {
                    _api.value = OrtApi.EMPTY
                    _state.value = OrtApiState.UNINITIALIZED
                } else {
                    _api.value = api
                    _state.value = OrtApiState.READY
                }
            }
        }
    }

    suspend fun loadOrtResult(file: File) {
        mutex.withLock {
            withContext(Dispatchers.IO + NonCancellable) {
                _ortResultFile.value = file

                _state.value = OrtApiState.LOADING_RESULT

                runCatching {
                    _ortResult.value = file.readValue<OrtResult>().withResolvedScopes()
                }.onFailure {
                    _error.value = "Could not load ORT result file '${file.absolutePath}': ${it.message}"
                    _ortResult.value = OrtResult.EMPTY
                    _state.value = OrtApiState.UNINITIALIZED
                }
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
            settingsMapper.writeValue(settingsFile, settings)
        }.onFailure {
            _error.value = "Could not save settings at ${settingsFile.absolutePath}: ${it.message}"
        }
    }

    private fun createOrtApi(configDirPath: String, result: OrtResult): OrtApi? =
        runCatching {
            val configDir = File(configDirPath)

            val ortConfigFile = configDir.resolve(ORT_CONFIG_FILENAME)
            val config = ortConfigFile.takeIf { it.isFile }?.let { OrtConfiguration.load(file = it) }
                ?: OrtConfiguration()

            LicenseFilenamePatterns.configure(config.licenseFilePatterns)

            val copyrightGarbage =
                configDir.resolve(ORT_COPYRIGHT_GARBAGE_FILENAME).takeIf { it.isFile }?.readValue()
                    ?: CopyrightGarbage()
            val fileArchiver = config.scanner.archive.createFileArchiver()

            // TODO: Let ORT provide a default location for a package configuration file.
            val packageConfigurationsDir = configDir.resolve(ORT_PACKAGE_CONFIGURATIONS_DIRNAME)
            val packageConfigurationProvider = if (packageConfigurationsDir.isDirectory) {
                SimplePackageConfigurationProvider.forDirectory(packageConfigurationsDir)
            } else {
                SimplePackageConfigurationProvider.EMPTY
            }

            val licenseInfoProvider = DefaultLicenseInfoProvider(result, packageConfigurationProvider)
            val licenseInfoResolver = result.createLicenseInfoResolver(
                packageConfigurationProvider,
                copyrightGarbage,
                config.addAuthorsToCopyrights,
                fileArchiver
            )

            val resolutionsFile = configDir.resolve(ORT_RESOLUTIONS_FILENAME)
            val resolutionProvider = DefaultResolutionProvider.create(result, resolutionsFile)

            OrtApi(
                result,
                config,
                copyrightGarbage,
                fileArchiver,
                licenseInfoProvider,
                licenseInfoResolver,
                packageConfigurationProvider,
                resolutionProvider
            )
        }.onFailure {
            // TODO: Provide more context where exactly the error occurred, e.g. which config file failed to parse.
            _error.value = "Could not process ORT result: ${it.message}"
            LicenseFilenamePatterns.configure(LicenseFilenamePatterns.DEFAULT)
        }.getOrNull()
}

data class WorkbenchSettings(
    val ortConfigDir: String
) {
    companion object {
        fun default() = WorkbenchSettings(
            ortConfigDir = ortConfigDirectory.invariantSeparatorsPath
        )
    }
}

enum class OrtApiState {
    UNINITIALIZED,
    LOADING_RESULT,
    PROCESSING_RESULT,
    READY
}

class OrtApi(
    val result: OrtResult,
    val config: OrtConfiguration,
    val copyrightGarbage: CopyrightGarbage,
    val fileArchiver: FileArchiver,
    val licenseInfoProvider: LicenseInfoProvider,
    val licenseInfoResolver: LicenseInfoResolver,
    val packageConfigurationProvider: PackageConfigurationProvider,
    val resolutionProvider: ResolutionProvider
) {
    companion object {
        val EMPTY by lazy {
            val result = OrtResult.EMPTY
            val copyrightGarbage = CopyrightGarbage()
            val config = OrtConfiguration()
            val fileArchiver = config.scanner.archive.createFileArchiver()
            val packageConfigurationProvider = SimplePackageConfigurationProvider.EMPTY
            val licenseInfoProvider = DefaultLicenseInfoProvider(result, packageConfigurationProvider)

            OrtApi(
                result,
                config,
                copyrightGarbage,
                fileArchiver,
                licenseInfoProvider,
                result.createLicenseInfoResolver(
                    packageConfigurationProvider,
                    copyrightGarbage,
                    config.addAuthorsToCopyrights,
                    fileArchiver
                ),
                packageConfigurationProvider,
                DefaultResolutionProvider()
            )
        }
    }
}
