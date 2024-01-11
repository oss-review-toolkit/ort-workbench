package org.ossreviewtoolkit.workbench.ui.summary

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable

import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material.RichText

import java.time.Instant

import kotlin.time.Duration.Companion.minutes

import org.ossreviewtoolkit.reporter.IssueStatistics
import org.ossreviewtoolkit.workbench.composables.Preview
import org.ossreviewtoolkit.workbench.model.PackageManagerStats
import org.ossreviewtoolkit.workbench.model.ProjectStats
import org.ossreviewtoolkit.workbench.utils.OrtIcon

@Composable
fun AnalyzerInfoCard(info: AnalyzerInfo, startExpanded: Boolean = false) {
    ToolInfoCard(
        icon = OrtIcon.ANALYZER,
        toolName = "Analyzer",
        info = info,
        startExpanded = startExpanded
    ) {
        info.projectStats.packageManagerStats.forEach { (type, stats) ->
            RichText {
                Markdown(
                    "* Analyzed ${stats.projectCount} $type project(s) with ${stats.dependencyCount} dependencies."
                )
            }
        }
    }
}

@Composable
@Preview
private fun AnalyzerInfoCardPreview() {
    Preview {
        AnalyzerInfoCard(
            AnalyzerInfo(
                startTime = Instant.now(),
                duration = 1000.minutes,
                issueStats = IssueStatistics(0, 1, 2, 3),
                serializedConfig = "allow_dynamic_versions: false\nskip_excluded: false",
                environment = mapOf(
                    "ORT Version" to "2.0.0",
                    "Java Version" to "17.0.5",
                    "OS" to "Windows 10"
                ),
                environmentVariables = mapOf(
                    "TERM" to "xterm",
                    "JAVA_HOME" to "C:/Program Files/Eclipse Adoptium/jdk-17.0.5.8-hotspot/"
                ),
                toolVersions = mapOf(
                    "NPM" to "9.4.0"
                ),
                projectStats = ProjectStats(
                    packageManagerStats = mapOf(
                        "Gradle" to PackageManagerStats(1, 2),
                        "Maven" to PackageManagerStats(3, 4),
                        "NPM" to PackageManagerStats(5, 6)
                    )
                )
            ),
            startExpanded = true
        )
    }
}
