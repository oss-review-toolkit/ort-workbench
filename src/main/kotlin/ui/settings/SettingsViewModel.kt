package org.ossreviewtoolkit.workbench.ui.settings

import java.io.File
import java.nio.file.Path

import kotlin.io.path.invariantSeparatorsPathString

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import org.ossreviewtoolkit.utils.ort.ORT_CONFIG_FILENAME
import org.ossreviewtoolkit.utils.ort.ORT_COPYRIGHT_GARBAGE_FILENAME
import org.ossreviewtoolkit.utils.ort.ORT_CUSTOM_LICENSE_TEXTS_DIRNAME
import org.ossreviewtoolkit.utils.ort.ORT_EVALUATOR_RULES_FILENAME
import org.ossreviewtoolkit.utils.ort.ORT_HOW_TO_FIX_TEXT_PROVIDER_FILENAME
import org.ossreviewtoolkit.utils.ort.ORT_LICENSE_CLASSIFICATIONS_FILENAME
import org.ossreviewtoolkit.utils.ort.ORT_PACKAGE_CONFIGURATIONS_DIRNAME
import org.ossreviewtoolkit.utils.ort.ORT_PACKAGE_CURATIONS_FILENAME
import org.ossreviewtoolkit.utils.ort.ORT_RESOLUTIONS_FILENAME
import org.ossreviewtoolkit.workbench.model.OrtModel
import org.ossreviewtoolkit.workbench.model.WorkbenchSettings
import org.ossreviewtoolkit.workbench.model.WorkbenchTheme

class SettingsViewModel(private val ortModel: OrtModel) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _tab = MutableStateFlow(SettingsTab.CONFIG_FILES)
    val tab: StateFlow<SettingsTab> get() = _tab

    private val _ortConfigDir = MutableStateFlow(
        OrtConfigFileInfo(
            ORT_CONFIG_DIR,
            File(WorkbenchSettings.default().ortConfigDir).toFileInfo(FileType.DIRECTORY)
        )
    )
    val ortConfigDir: StateFlow<OrtConfigFileInfo> get() = _ortConfigDir

    private val _theme = MutableStateFlow(WorkbenchTheme.AUTO)
    val theme: StateFlow<WorkbenchTheme> get() = _theme

    private val _ortConfigFiles = MutableStateFlow(emptyList<OrtConfigFileInfo>())
    val ortConfigFiles: StateFlow<List<OrtConfigFileInfo>> get() = _ortConfigFiles

    init {
        scope.launch {
            ortModel.settings.collect { settings ->
                val configDir = File(settings.ortConfigDir)

                _ortConfigDir.value = OrtConfigFileInfo(
                    ORT_CONFIG_DIR,
                    configDir.toFileInfo(FileType.DIRECTORY)
                )

                _ortConfigFiles.value = ORT_CONFIG_FILES.map { configFile ->
                    OrtConfigFileInfo(
                        configFile,
                        configDir.resolve(configFile.fileName).toFileInfo(configFile.fileType)
                    )
                }

                _theme.value = settings.theme
            }
        }
    }

    suspend fun setConfigDir(path: Path) {
        ortModel.updateSettings(ortModel.settings.value.copy(ortConfigDir = path.invariantSeparatorsPathString))
    }

    fun setTab(tab: SettingsTab) {
        _tab.value = tab
    }

    suspend fun setTheme(theme: WorkbenchTheme) {
        ortModel.updateSettings(ortModel.settings.value.copy(theme = theme))
    }
}

data class OrtConfigFile(
    val name: String,
    val description: String,
    val documentationUrl: String,
    val exampleUrl: String? = null,
    val fileName: String,
    val fileType: FileType
)

private val ORT_CONFIG_DIR = OrtConfigFile(
    name = "ORT configuration directory",
    description = "The directory that contains the ORT configuration files. The ORT workbench currently requires " +
            "that all configuration files use their default file names.",
    documentationUrl = "https://github.com/oss-review-toolkit/ort#configuration-files",
    fileName = "config", // TODO: ORT should provide a constant for that.
    fileType = FileType.DIRECTORY
)

private val ORT_CONFIG_FILES = listOf(
    OrtConfigFile(
        name = "ORT config file",
        description = "The main ORT configuration file, contains settings for all ORT tools.",
        documentationUrl = "https://github.com/oss-review-toolkit/ort#ort-configuration-file",
        exampleUrl = "https://github.com/oss-review-toolkit/ort/blob/master/model/src/main/resources/reference.conf",
        fileName = ORT_CONFIG_FILENAME,
        fileType = FileType.FILE
    ),
    OrtConfigFile(
        name = "Copyright garbage file",
        description = "A list of copyright statements that are considered garbage, for example statements that were " +
                "incorrectly classified as copyrights by a scanner.",
        documentationUrl = "https://github.com/oss-review-toolkit/ort/blob/master/docs/" +
                "config-file-copyright-garbage-yml.md",
        exampleUrl = "https://github.com/oss-review-toolkit/ort/blob/master/examples/copyright-garbage.yml",
        fileName = ORT_COPYRIGHT_GARBAGE_FILENAME,
        fileType = FileType.FILE
    ),
    OrtConfigFile(
        name = "Curations file",
        description = "A file to correct invalid or missing package metadata, and to set the concluded license for " +
                "packages.",
        documentationUrl = "https://github.com/oss-review-toolkit/ort/blob/master/docs/config-file-curations-yml.md",
        exampleUrl = "https://github.com/oss-review-toolkit/ort/blob/master/examples/curations.yml",
        fileName = ORT_PACKAGE_CURATIONS_FILENAME,
        fileType = FileType.FILE
    ),
    OrtConfigFile(
        name = "Curations directory",
        description = "A directory containing package curation files.",
        documentationUrl = "https://github.com/oss-review-toolkit/ort/blob/master/docs/config-file-curations-yml.md",
        fileName = ORT_PACKAGE_CONFIGURATIONS_DIRNAME,
        fileType = FileType.DIRECTORY
    ),
    OrtConfigFile(
        name = "Custom license texts directory",
        description = "A directory that contains license texts which are not provided by ORT.",
        documentationUrl = "https://github.com/oss-review-toolkit/ort/blob/master/docs/dir-custom-license-texts.md",
        fileName = ORT_CUSTOM_LICENSE_TEXTS_DIRNAME,
        fileType = FileType.DIRECTORY
    ),
    OrtConfigFile(
        name = "How to fix text provider script",
        description = "A Kotlin script that enables the injection of how-to-fix texts in markdown format for ORT " +
                "issues into the reports.",
        documentationUrl = "https://github.com/oss-review-toolkit/ort/blob/master/docs/how-to-fix-text-provider-kts.md",
        exampleUrl = "https://github.com/oss-review-toolkit/ort/blob/master/examples/how-to-fix-text-provider.kts",
        fileName = ORT_HOW_TO_FIX_TEXT_PROVIDER_FILENAME,
        fileType = FileType.FILE
    ),
    OrtConfigFile(
        name = "License classifications file",
        description = "A file that contains user-defined categorization of licenses.",
        documentationUrl = "https://github.com/oss-review-toolkit/ort/blob/master/docs/" +
                "config-file-license-classifications-yml.md",
        exampleUrl = "https://github.com/oss-review-toolkit/ort/blob/master/examples/license-classifications.yml",
        fileName = ORT_LICENSE_CLASSIFICATIONS_FILENAME,
        fileType = FileType.FILE
    ),
    OrtConfigFile(
        name = "Resolutions file",
        description = "Configurations to resolve any issues or rule violations by providing a mandatory reason, and " +
                "an optional comment to justify the resolution on a global scale.",
        documentationUrl = "https://github.com/oss-review-toolkit/ort/blob/master/docs/config-file-resolutions-yml.md",
        exampleUrl = "https://github.com/oss-review-toolkit/ort/blob/master/examples/resolutions.yml",
        fileName = ORT_RESOLUTIONS_FILENAME,
        fileType = FileType.FILE
    ),
    OrtConfigFile(
        name = "Package configuration file",
        description = "A file containing configurations to set provenance-specific path excludes and license finding " +
                "curations for dependency packages to address issues found within a scan result.",
        documentationUrl = "https://github.com/oss-review-toolkit/ort/blob/master/docs/" +
                "config-file-package-configuration-yml.md",
        exampleUrl = "https://github.com/oss-review-toolkit/ort/blob/master/examples/package-configurations.ort.yml",
        fileName = "package-configurations.yml", // TODO: ORT should provide a constant for that.
        fileType = FileType.FILE
    ),
    OrtConfigFile(
        name = "Package configuration directory",
        description = "A directory containing package configuration files.",
        documentationUrl = "https://github.com/oss-review-toolkit/ort/blob/master/docs/" +
                "config-file-package-configuration-yml.md",
        fileName = ORT_PACKAGE_CONFIGURATIONS_DIRNAME,
        fileType = FileType.DIRECTORY
    ),
    OrtConfigFile(
        name = "Policy rules file",
        description = "A file containing policy rule implementations to be used with the evaluator.",
        documentationUrl = "https://github.com/oss-review-toolkit/ort/blob/master/docs/file-rules-kts.md",
        exampleUrl = "https://github.com/oss-review-toolkit/ort/blob/master/examples/evaluator-rules/src/main/" +
                "resources/example.rules.kts",
        fileName = ORT_EVALUATOR_RULES_FILENAME,
        fileType = FileType.FILE
    )
)

data class FileInfo(
    val file: File,
    val type: FileType,
    val exists: Boolean,
    val errorMessage: String?,
)

enum class FileType { DIRECTORY, FILE }

fun File.toFileInfo(type: FileType): FileInfo {
    val (exists, errorMessage) = when (type) {
        FileType.DIRECTORY -> when {
            isDirectory -> true to null
            isFile -> false to "Is a file but should be a directory."
            else -> false to "Directory not found."
        }

        FileType.FILE -> when {
            isFile -> true to null
            isDirectory -> false to "Is a directory but should be a file."
            else -> false to "File not found."
        }
    }

    return FileInfo(this, type, exists, errorMessage)
}

data class OrtConfigFileInfo(
    val configFile: OrtConfigFile,
    val fileInfo: FileInfo
)
