package org.ossreviewtoolkit.workbench.ui.summary

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material.RichText

import java.time.Instant

import kotlin.time.Duration.Companion.minutes

import org.ossreviewtoolkit.model.ScannerDetails
import org.ossreviewtoolkit.reporter.IssueStatistics
import org.ossreviewtoolkit.workbench.composables.Preview
import org.ossreviewtoolkit.workbench.model.ScannerStats
import org.ossreviewtoolkit.workbench.model.ScannerWrapperStats
import org.ossreviewtoolkit.workbench.utils.OrtIcon

@Composable
fun ScannerInfoCard(info: ScannerInfo?, startExpanded: Boolean = false) {
    if (info == null) {
        EmptyToolInfoCard(icon = OrtIcon.SCANNER, toolName = "Scanner")
    } else {
        ToolInfoCard(
            icon = OrtIcon.SCANNER,
            toolName = "Scanner",
            info = info,
            startExpanded = startExpanded
        ) {
            info.scannerStats.scannerWrapperStats.forEach { (scanner, stats) ->
                val markdown = with(stats) {
                    """
                        * Scanned $scannedPackageCount package(s) with ${scanner.name} ${scanner.version}.
                          Detected $detectedLicenseCount licenses and $detectedCopyrightCount copyrights in
                          $scannedSourceArtifactCount source artifacts and $scannedRepositoryCount source code
                          repositories.
                    """.trimIndent()
                }

                RichText { Markdown(markdown) }
            }
        }
    }
}

@Composable
@Preview
private fun ScannerInfoCardPreview() {
    Preview {
        ScannerInfoCard(
            ScannerInfo(
                startTime = Instant.now(),
                duration = 1000.minutes,
                issueStats = IssueStatistics(0, 1, 2, 3),
                serializedConfig = "create_missing_archives: false\nskip_concluded: false",
                environment = mapOf(
                    "ORT Version" to "2.0.0",
                    "Java Version" to "17.0.5",
                    "OS" to "Windows 10"
                ),
                environmentVariables = mapOf(
                    "TERM" to "xterm",
                    "JAVA_HOME" to "C:/Program Files/Eclipse Adoptium/jdk-17.0.5.8-hotspot/"
                ),
                scannerStats = ScannerStats(
                    scannerWrapperStats = mapOf(
                        ScannerDetails(name = "ScanCode", version = "32.0.6", configuration = "") to
                                ScannerWrapperStats(1, 2, 3, 4, 5),
                        ScannerDetails(name = "ScanCode", version = "32.0.7", configuration = "") to
                                ScannerWrapperStats(6, 7, 8, 9, 10)
                    )
                )
            ),
            startExpanded = true
        )
    }
}
