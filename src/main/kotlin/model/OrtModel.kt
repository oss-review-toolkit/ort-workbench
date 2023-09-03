package org.ossreviewtoolkit.workbench.model

import java.io.File

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

import org.apache.logging.log4j.kotlin.Logging

import org.ossreviewtoolkit.model.OrtResult
import org.ossreviewtoolkit.model.config.CopyrightGarbage
import org.ossreviewtoolkit.model.config.FileArchiverConfiguration
import org.ossreviewtoolkit.model.config.LicenseFilePatterns
import org.ossreviewtoolkit.model.config.OrtConfiguration
import org.ossreviewtoolkit.model.config.createFileArchiver
import org.ossreviewtoolkit.model.licenses.DefaultLicenseInfoProvider
import org.ossreviewtoolkit.model.readValue
import org.ossreviewtoolkit.model.utils.CompositePackageConfigurationProvider
import org.ossreviewtoolkit.model.utils.DefaultResolutionProvider
import org.ossreviewtoolkit.model.utils.createLicenseInfoResolver
import org.ossreviewtoolkit.plugins.packageconfigurationproviders.api.SimplePackageConfigurationProvider
import org.ossreviewtoolkit.plugins.packageconfigurationproviders.dir.DirPackageConfigurationProvider
import org.ossreviewtoolkit.utils.ort.ORT_CONFIG_FILENAME
import org.ossreviewtoolkit.utils.ort.ORT_COPYRIGHT_GARBAGE_FILENAME
import org.ossreviewtoolkit.utils.ort.ORT_PACKAGE_CONFIGURATIONS_DIRNAME
import org.ossreviewtoolkit.utils.ort.ORT_RESOLUTIONS_FILENAME
import org.ossreviewtoolkit.utils.ort.ortConfigDirectory
import org.ossreviewtoolkit.workbench.navigation.NavController
import org.ossreviewtoolkit.workbench.ui.MainScreen

class OrtModel(val settings: StateFlow<WorkbenchSettings>) {
    companion object : Logging

    private val mutex = Mutex()

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _state = MutableStateFlow(OrtApiState.UNINITIALIZED)
    val state: StateFlow<OrtApiState> = _state

    private val _ortResultFile: MutableStateFlow<File?> = MutableStateFlow(null)
    val ortResultFile: StateFlow<File?> = _ortResultFile

    private val _ortResult = MutableStateFlow<OrtResult?>(null)

    private val _info = MutableStateFlow<OrtModelInfo?>(null)
    val info: StateFlow<OrtModelInfo?> = _info

    private val _api = MutableStateFlow(OrtApi.EMPTY)
    val api: StateFlow<OrtApi> = _api

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _useOnlyResolvedConfiguration = MutableStateFlow(true)
    val useOnlyResolvedConfiguration: StateFlow<Boolean> = _useOnlyResolvedConfiguration

    val navController = NavController(
        MainScreen.Dependencies(this),
        MainScreen.Issues(this),
        MainScreen.Packages(this),
        MainScreen.RuleViolations(this),
        MainScreen.Vulnerabilities(this),
        MainScreen.Summary(this)
    )

    init {
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

        scope.launch {
            combine(ortResultFile, _ortResult) { resultFile, result ->
                val projectsByPackageManager =
                    result?.getProjects()?.groupBy { it.id.type }?.mapValues { it.value.size }.orEmpty()

                val name = result?.repository?.vcs?.url?.substringAfterLast("/")?.removeSuffix(".git")
                    ?: result?.getProjects(omitExcluded = true)
                        ?.minByOrNull { project -> project.definitionFilePath.count { it == '/' } }
                        ?.id?.name
                    ?: resultFile?.parentFile?.name
                    ?: "<unknown>"

                OrtModelInfo(
                    name = name,
                    filePath = resultFile?.absolutePath.orEmpty(),
                    fileSize = resultFile?.length() ?: 0L,
                    projectsByPackageManager = projectsByPackageManager
                )
            }.collect {
                _info.value = it
            }
        }
    }

    suspend fun loadOrtResult(file: File) {
        mutex.withLock {
            withContext(Dispatchers.IO + NonCancellable) {
                _ortResultFile.value = file

                _state.value = OrtApiState.LOADING_RESULT

                // Reset the _ortResult to null before assigning a new value, otherwise no new value is emitted if the
                // same file is loaded again.
                _ortResult.value = null

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

    suspend fun switchUseOnlyResolvedConfiguration() {
        _useOnlyResolvedConfiguration.value = !_useOnlyResolvedConfiguration.value
        _ortResult.value?.let { ortResult ->
            _state.value = OrtApiState.PROCESSING_RESULT
            val newApi = createOrtApi(settings.value.ortConfigDir, ortResult)
            if (newApi == null) {
                _api.value = OrtApi.EMPTY
                _state.value = OrtApiState.UNINITIALIZED
            } else {
                _api.value = newApi
                _state.value = OrtApiState.READY
            }
        }
    }

    private fun createOrtApi(configDirPath: String, result: OrtResult): OrtApi? =
        runCatching {
            val configDir = File(configDirPath)

            val ortConfigFile = configDir.resolve(ORT_CONFIG_FILENAME)
            val config = ortConfigFile.takeIf { it.isFile }?.let { OrtConfiguration.load(file = it) }
                ?: OrtConfiguration()

            LicenseFilePatterns.configure(config.licenseFilePatterns)

            val copyrightGarbage =
                configDir.resolve(ORT_COPYRIGHT_GARBAGE_FILENAME).takeIf { it.isFile }?.readValue()
                    ?: CopyrightGarbage()

            val fileArchiver = runCatching {
                config.scanner.archive.createFileArchiver()
            }.getOrElse {
                logger.warn {
                    "Failed to create the configured scanner file archiver, falling back to the default one."
                }

                FileArchiverConfiguration().createFileArchiver()
            }

            val resolvedPackageConfigurationProvider =
                SimplePackageConfigurationProvider(result.resolvedConfiguration.packageConfigurations.orEmpty())
            val packageConfigurationsDir = configDir.resolve(ORT_PACKAGE_CONFIGURATIONS_DIRNAME)

            val packageConfigurationProvider =
                if (useOnlyResolvedConfiguration.value || !packageConfigurationsDir.isDirectory) {
                    resolvedPackageConfigurationProvider
                } else {
                    CompositePackageConfigurationProvider(
                        resolvedPackageConfigurationProvider,
                        DirPackageConfigurationProvider(packageConfigurationsDir)
                    )
                }

            val licenseInfoProvider = DefaultLicenseInfoProvider(result, packageConfigurationProvider)
            val licenseInfoResolver = result.createLicenseInfoResolver(
                packageConfigurationProvider,
                copyrightGarbage,
                config.addAuthorsToCopyrights,
                fileArchiver
            )

            val resolutionsFile = configDir.resolve(ORT_RESOLUTIONS_FILENAME)
            val resolutionProvider = DefaultResolutionProvider.create(
                result,
                resolutionsFile.takeUnless { useOnlyResolvedConfiguration.value }
            )

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
            LicenseFilePatterns.configure(LicenseFilePatterns.DEFAULT)
        }.getOrNull()
}

data class WorkbenchSettings(
    val ortConfigDir: String,
    val theme: WorkbenchTheme = WorkbenchTheme.AUTO
) {
    companion object {
        fun default() = WorkbenchSettings(
            ortConfigDir = ortConfigDirectory.invariantSeparatorsPath,
            theme = WorkbenchTheme.AUTO
        )
    }
}

enum class OrtApiState {
    UNINITIALIZED,
    LOADING_RESULT,
    PROCESSING_RESULT,
    READY
}

enum class WorkbenchTheme {
    AUTO, LIGHT, DARK
}
