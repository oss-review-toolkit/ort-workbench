package org.ossreviewtoolkit.workbench.ui.summary

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable

import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material.RichText

import java.time.Instant

import kotlin.time.Duration.Companion.minutes

import org.ossreviewtoolkit.model.LicenseSource
import org.ossreviewtoolkit.reporter.IssueStatistics
import org.ossreviewtoolkit.workbench.composables.Preview
import org.ossreviewtoolkit.workbench.model.EvaluatorStats
import org.ossreviewtoolkit.workbench.utils.OrtIcon

@Composable
fun EvaluatorInfoCard(info: EvaluatorInfo?, startExpanded: Boolean = false) {
    if (info == null) {
        EmptyToolInfoCard(icon = OrtIcon.EVALUATOR, toolName = "Evaluator")
    } else {
        ToolInfoCard(
            icon = OrtIcon.EVALUATOR,
            toolName = "Evaluator",
            info = info,
            startExpanded = startExpanded
        ) {
            val markdown = """
                * Found a total of ${info.evaluatorStats.ruleViolationCount} rule violation(s) of which
                  ${info.evaluatorStats.ruleViolationCountByLicenseSource[LicenseSource.CONCLUDED] ?: 0} were triggered
                  by concluded licenses,
                  ${info.evaluatorStats.ruleViolationCountByLicenseSource[LicenseSource.DECLARED] ?: 0} by declared
                  licenses, and ${info.evaluatorStats.ruleViolationCountByLicenseSource[LicenseSource.DETECTED] ?: 0} by
                  detected licenses.
                * Found ${info.evaluatorStats.packageWithRuleViolationCount} different package(s) with rule violations.
                * A total of ${info.evaluatorStats.ruleThatTriggeredViolationCount} different rule(s) were violated.
            """.trimIndent()

            RichText { Markdown(markdown) }
        }
    }
}

@Composable
@Preview
private fun EvaluatorInfoCardPreview() {
    Preview {
        EvaluatorInfoCard(
            EvaluatorInfo(
                startTime = Instant.now(),
                duration = 1000.minutes,
                issueStats = IssueStatistics(0, 1, 2, 3),
                evaluatorStats = EvaluatorStats(
                    ruleViolationCount = 1,
                    ruleViolationCountByLicenseSource = mapOf(
                        LicenseSource.CONCLUDED to 2,
                        LicenseSource.DECLARED to 3,
                        LicenseSource.DETECTED to 4
                    ),
                    packageWithRuleViolationCount = 5,
                    ruleThatTriggeredViolationCount = 6
                )
            ),
            startExpanded = true
        )
    }
}
