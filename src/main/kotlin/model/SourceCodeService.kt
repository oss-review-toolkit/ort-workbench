package org.ossreviewtoolkit.workbench.model

import java.io.File
import java.util.concurrent.ConcurrentHashMap

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

import org.apache.logging.log4j.kotlin.logger

import org.ossreviewtoolkit.downloader.Downloader
import org.ossreviewtoolkit.model.ArtifactProvenance
import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.KnownProvenance
import org.ossreviewtoolkit.model.Package
import org.ossreviewtoolkit.model.RepositoryProvenance
import org.ossreviewtoolkit.model.config.DownloaderConfiguration

/**
 * Service for downloading and caching source code from both source artifacts and VCS repositories,
 * and extracting specific file content from them. Downloads are cached in a temp directory keyed by
 * provenance to avoid re-downloading.
 */
class SourceCodeService(downloaderConfiguration: DownloaderConfiguration = DownloaderConfiguration()) {
    private val downloader = Downloader(downloaderConfiguration)

    private val cacheDir = File(System.getProperty("java.io.tmpdir"), "ort-workbench-sources").apply { mkdirs() }
    private val extractedDirs = ConcurrentHashMap<String, File>()
    private val downloadMutexes = ConcurrentHashMap<String, Mutex>()

    /**
     * Get the source code lines for a file within a downloaded source.
     *
     * @param provenance The provenance (artifact or VCS) of the source code.
     * @param packageId The package identifier, required for VCS downloads.
     * @param path The relative file path within the source.
     * @param startLine The 1-based start line.
     * @param endLine The 1-based end line.
     * @param contextLines Number of extra lines to include before and after the finding.
     * @return A [SourceCodeResult] with the source lines and actual line range.
     */
    suspend fun getSourceLines(
        provenance: KnownProvenance,
        packageId: Identifier,
        path: String,
        startLine: Int,
        endLine: Int,
        contextLines: Int = CONTEXT_LINES
    ): Result<SourceCodeResult> = withContext(Dispatchers.IO) {
        runCatching {
            val extractDir = ensureDownloaded(provenance, packageId)
            val sourceFile = extractDir.resolve(path)

            require(sourceFile.isFile) { "File '$path' not found in source." }

            val allLines = sourceFile.readLines()
            val contextStart = maxOf(1, startLine - contextLines)
            val contextEnd = minOf(allLines.size, endLine + contextLines)

            val lines = allLines.subList(contextStart - 1, contextEnd)

            SourceCodeResult(
                lines = lines,
                firstLineNumber = contextStart,
                findingStartLine = startLine,
                findingEndLine = endLine
            )
        }
    }

    private suspend fun ensureDownloaded(provenance: KnownProvenance, packageId: Identifier): File {
        val cacheKey = provenance.cacheKey()

        extractedDirs[cacheKey]?.let { return it }

        val mutex = downloadMutexes.getOrPut(cacheKey) { Mutex() }

        return mutex.withLock {
            extractedDirs.getOrPut(cacheKey) {
                val extractDir = cacheDir.resolve(cacheKey).apply { mkdirs() }

                if (extractDir.list()?.isEmpty() != false) {
                    downloadSource(provenance, packageId, extractDir)
                }

                extractDir
            }
        }
    }

    private fun downloadSource(provenance: KnownProvenance, packageId: Identifier, outputDir: File) {
        when (provenance) {
            is ArtifactProvenance -> {
                logger.info { "Downloading source artifact: ${provenance.sourceArtifact.url}" }
                downloader.downloadSourceArtifact(provenance.sourceArtifact, outputDir)
            }

            is RepositoryProvenance -> {
                logger.info { "Downloading VCS source: ${provenance.vcsInfo.url}@${provenance.resolvedRevision}" }

                val pkg = Package.EMPTY.copy(id = packageId, vcsProcessed = provenance.vcsInfo)
                downloader.downloadFromVcs(pkg, outputDir)
            }
        }
    }

    fun cleanup() {
        extractedDirs.clear()
        cacheDir.deleteRecursively()
    }
}

private const val HEX_RADIX = 16

private fun KnownProvenance.cacheKey(): String = when (this) {
    is ArtifactProvenance -> sourceArtifact.hash.value.ifEmpty {
        sourceArtifact.url.hashCode().toUInt().toString(HEX_RADIX)
    }

    is RepositoryProvenance -> {
        val urlHash = vcsInfo.url.hashCode().toUInt().toString(HEX_RADIX)
        "${urlHash}_$resolvedRevision"
    }
}

/**
 * Result of reading source code from a downloaded source.
 */
data class SourceCodeResult(
    val lines: List<String>,
    val firstLineNumber: Int,
    val findingStartLine: Int,
    val findingEndLine: Int,
    val detectedLicense: String? = null
)

private const val CONTEXT_LINES = 5
