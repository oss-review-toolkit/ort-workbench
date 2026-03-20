package org.ossreviewtoolkit.workbench.model

import java.io.File

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import org.apache.logging.log4j.kotlin.logger

import org.ossreviewtoolkit.model.ArtifactProvenance
import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.KnownProvenance
import org.ossreviewtoolkit.model.RepositoryProvenance
import org.ossreviewtoolkit.model.config.LicenseFindingCuration
import org.ossreviewtoolkit.model.config.LicenseFindingCurationReason
import org.ossreviewtoolkit.model.config.PackageConfiguration
import org.ossreviewtoolkit.model.config.RepositoryConfiguration
import org.ossreviewtoolkit.model.config.VcsMatcher
import org.ossreviewtoolkit.model.readValue
import org.ossreviewtoolkit.model.yamlMapper
import org.ossreviewtoolkit.utils.spdx.SpdxExpression

private const val SOURCE_ARTIFACT_FILENAME = "source-artifact.yml"
private const val VCS_FILENAME = "vcs.yml"

/**
 * Service for accumulating license finding curations and saving them as ORT-compatible package configuration YAML
 * files in the hierarchical directory format: `{type}/{namespace}/{name}/{version}/{source-artifact|vcs}.yml`.
 */
class CurationService {
    internal data class PackageKey(val id: Identifier, val provenance: KnownProvenance)

    private val curations = mutableMapOf<PackageKey, MutableList<LicenseFindingCuration>>()
    private val curatedFindingKeys = mutableSetOf<String>()

    private val _curationCount = MutableStateFlow(0)
    val curationCount: StateFlow<Int> = _curationCount

    fun addCuration(id: Identifier, provenance: KnownProvenance, findingKey: String, curation: LicenseFindingCuration) {
        val key = PackageKey(id, provenance)

        curations.getOrPut(key) { mutableListOf() }.add(curation)
        curatedFindingKeys.add(findingKey)
        _curationCount.value = curations.values.sumOf { it.size }

        logger.info { "Added curation for $findingKey in ${id.toCoordinates()}" }
    }

    fun removeCuration(id: Identifier, provenance: KnownProvenance, findingKey: String) {
        val key = PackageKey(id, provenance)

        curations[key]?.removeAll { it.matchesFindingKey(findingKey) }

        if (curations[key]?.isEmpty() == true) {
            curations.remove(key)
        }

        curatedFindingKeys.remove(findingKey)
        _curationCount.value = curations.values.sumOf { it.size }

        logger.info { "Removed curation for $findingKey in ${id.toCoordinates()}" }
    }

    fun hasCuration(findingKey: String): Boolean = findingKey in curatedFindingKeys

    /**
     * Save all pending curations to the package configurations directory using the hierarchical format:
     * `baseDir/{type}/{namespace}/{name}/{version}/{source-artifact|vcs}.yml`.
     *
     * If a file already exists, the new curations are merged into it. Duplicate curations (same path + startLines)
     * are skipped.
     */
    fun saveToPackageConfigDir(baseDir: File): Result<Int> = runCatching {
        var fileCount = 0

        curations.forEach { (key, newCurations) ->
            val file = resolveConfigFile(baseDir, key.id, key.provenance)
            file.parentFile.mkdirs()

            val mergedConfig = if (file.isFile) {
                mergeWithExisting(file, newCurations)
            } else {
                createNewConfig(key, newCurations)
            }

            yamlMapper.writeValue(file, mergedConfig)
            fileCount++

            logger.info { "Saved package configuration to ${file.absolutePath}" }
        }

        fileCount
    }

    /**
     * Export all pending curations as `package_configurations` in a `.ort.yml` file. If the file already exists,
     * the new package configurations are merged into the existing `RepositoryConfiguration`.
     */
    fun exportToOrtYml(file: File): Result<Int> = runCatching {
        val newConfigs = buildPackageConfigurations()

        val repoConfig = if (file.isFile) {
            mergeIntoRepoConfig(file, newConfigs)
        } else {
            RepositoryConfiguration(packageConfigurations = newConfigs)
        }

        file.parentFile?.mkdirs()
        yamlMapper.writeValue(file, repoConfig)

        logger.info { "Exported ${newConfigs.size} package configuration(s) to ${file.absolutePath}" }

        newConfigs.size
    }

    internal fun buildPackageConfigurations(): List<PackageConfiguration> =
        curations.map { (key, curationList) -> createNewConfig(key, curationList) }

    fun clear() {
        curations.clear()
        curatedFindingKeys.clear()
        _curationCount.value = 0
    }
}

/**
 * Build the hierarchical file path for a package configuration:
 * `{type}/{namespace}/{name}/{version}/{source-artifact|vcs}.yml`.
 */
private fun resolveConfigFile(baseDir: File, id: Identifier, provenance: KnownProvenance): File {
    var dir = baseDir.resolve(id.type)

    if (id.namespace.isNotEmpty()) {
        dir = dir.resolve(id.namespace)
    }

    val filename = when (provenance) {
        is ArtifactProvenance -> SOURCE_ARTIFACT_FILENAME
        is RepositoryProvenance -> VCS_FILENAME
    }

    return dir.resolve(id.name).resolve(id.version).resolve(filename)
}

private fun mergeWithExisting(file: File, newCurations: List<LicenseFindingCuration>): PackageConfiguration {
    val existing = file.readValue<PackageConfiguration>()
    val existingCurations = existing.licenseFindingCurations
    val merged = existingCurations + newCurations.filter { new -> !existingCurations.isDuplicate(new) }

    return existing.copy(licenseFindingCurations = merged)
}

private fun createNewConfig(
    key: CurationService.PackageKey,
    curations: List<LicenseFindingCuration>
): PackageConfiguration {
    val (sourceArtifactUrl, vcs) = when (val provenance = key.provenance) {
        is ArtifactProvenance -> provenance.sourceArtifact.url to null
        is RepositoryProvenance -> null to VcsMatcher(
            type = provenance.vcsInfo.type,
            url = provenance.vcsInfo.url,
            revision = provenance.resolvedRevision
        )
    }

    return PackageConfiguration(
        id = key.id,
        sourceArtifactUrl = sourceArtifactUrl,
        vcs = vcs,
        licenseFindingCurations = curations.toList()
    )
}

private fun List<LicenseFindingCuration>.isDuplicate(other: LicenseFindingCuration): Boolean =
    any { it.path == other.path && it.startLines == other.startLines && it.lineCount == other.lineCount }

private fun mergeIntoRepoConfig(file: File, newConfigs: List<PackageConfiguration>): RepositoryConfiguration {
    val existing = file.readValue<RepositoryConfiguration>()
    val merged = existing.packageConfigurations.toMutableList()

    newConfigs.forEach { newConfig ->
        val existingIndex = merged.indexOfFirst { it.id == newConfig.id && it.matches(newConfig) }

        if (existingIndex >= 0) {
            val existingConfig = merged[existingIndex]
            val mergedCurations = existingConfig.licenseFindingCurations +
                newConfig.licenseFindingCurations.filter { new ->
                    !existingConfig.licenseFindingCurations.isDuplicate(new)
                }

            merged[existingIndex] = existingConfig.copy(licenseFindingCurations = mergedCurations)
        } else {
            merged.add(newConfig)
        }
    }

    return existing.copy(packageConfigurations = merged)
}

/**
 * Check if two [PackageConfiguration]s match based on their provenance (source artifact URL or VCS matcher).
 */
private fun PackageConfiguration.matches(other: PackageConfiguration): Boolean =
    (sourceArtifactUrl != null && sourceArtifactUrl == other.sourceArtifactUrl) ||
        (vcs != null && vcs == other.vcs)

/**
 * Check if a [LicenseFindingCuration] matches a finding key of the form "prefix:path:startLine-endLine".
 */
private fun LicenseFindingCuration.matchesFindingKey(findingKey: String): Boolean {
    val pathAndRange = ":$path:$startLines"

    return findingKey.contains(pathAndRange)
}

/**
 * Create a [LicenseFindingCuration] from the details of a license finding.
 */
fun createLicenseFindingCuration(
    path: String,
    startLine: Int,
    endLine: Int,
    detectedLicense: String,
    concludedLicense: String,
    reason: LicenseFindingCurationReason,
    comment: String
): LicenseFindingCuration {
    val lineCount = endLine - startLine + 1

    return LicenseFindingCuration(
        path = path,
        startLines = listOf(startLine),
        lineCount = lineCount,
        detectedLicense = SpdxExpression.parse(detectedLicense),
        concludedLicense = SpdxExpression.parse(concludedLicense),
        reason = reason,
        comment = comment
    )
}
