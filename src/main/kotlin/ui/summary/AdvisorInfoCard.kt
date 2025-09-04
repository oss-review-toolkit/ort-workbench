package org.ossreviewtoolkit.workbench.ui.summary

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable

import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material.RichText

import java.time.Instant

import kotlin.time.Duration.Companion.minutes

import org.ossreviewtoolkit.reporter.IssueStatistics
import org.ossreviewtoolkit.workbench.composables.Preview
import org.ossreviewtoolkit.workbench.model.AdviceProviderStats
import org.ossreviewtoolkit.workbench.model.AdvisorStats
import org.ossreviewtoolkit.workbench.utils.OrtIcon

@Composable
fun AdvisorInfoCard(info: AdvisorInfo?, startExpanded: Boolean = false) {
    if (info == null) {
        EmptyToolInfoCard(icon = OrtIcon.ADVISOR, toolName = "Advisor")
    } else {
        ToolInfoCard(
            icon = OrtIcon.ADVISOR,
            toolName = "Advisor",
            info = info,
            startExpanded = startExpanded
        ) {
            info.advisorStats.adviceProviderStats.forEach { (provider, stats) ->
                val markdown = """
                    * Requested vulnerabilities for ${stats.requestedPackageCount} packages from $provider. Found
                      ${stats.packageWithVulnerabilityCount} vulnerable package(s) with a total of
                      ${stats.totalVulnerabilityCount} vulnerabilities.
                """.trimIndent()

                RichText { Markdown(markdown) }
            }
        }
    }
}

@Composable
@Preview
private fun AdvisorInfoCardPreview() {
    Preview {
        AdvisorInfoCard(
            AdvisorInfo(
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
                advisorStats = AdvisorStats(
                    adviceProviderStats = mapOf(
                        "OSV" to AdviceProviderStats(1, 2, 3),
                        "VulnerableCode" to AdviceProviderStats(4, 5, 6)
                    )
                )
            ),
            startExpanded = true
        )
    }
}
