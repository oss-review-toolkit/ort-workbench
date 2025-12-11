package org.ossreviewtoolkit.workbench.ui.violations

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.LicenseSource
import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.model.config.RuleViolationResolution
import org.ossreviewtoolkit.model.config.RuleViolationResolutionReason
import org.ossreviewtoolkit.utils.common.titlecase
import org.ossreviewtoolkit.utils.spdx.SpdxLicenseIdExpression
import org.ossreviewtoolkit.utils.spdx.SpdxSingleLicenseExpression
import org.ossreviewtoolkit.workbench.composables.CircularProgressBox
import org.ossreviewtoolkit.workbench.composables.ExpandableMarkdown
import org.ossreviewtoolkit.workbench.composables.ExpandableText
import org.ossreviewtoolkit.workbench.composables.FilterButton
import org.ossreviewtoolkit.workbench.composables.FilterPanel
import org.ossreviewtoolkit.workbench.composables.ListScreenContent
import org.ossreviewtoolkit.workbench.composables.ListScreenList
import org.ossreviewtoolkit.workbench.composables.Preview
import org.ossreviewtoolkit.workbench.composables.SeverityIcon
import org.ossreviewtoolkit.workbench.model.ResolutionStatus
import org.ossreviewtoolkit.workbench.model.ResolvedRuleViolation

@Composable
@Preview
fun Violations(viewModel: ViolationsViewModel) {
    val stateState = viewModel.state.collectAsState()

    when (val state = stateState.value) {
        is ViolationsState.Loading -> CircularProgressBox()

        is ViolationsState.Success -> {
            ListScreenContent(
                filterText = state.filter.text,
                onUpdateFilterText = viewModel::updateTextFilter,
                list = {
                    ListScreenList(
                        items = state.violations,
                        itemsEmptyText = "No violations found.",
                        item = { ViolationCard(it) }
                    )
                },
                filterPanel = { showFilterPanel ->
                    ViolationsFilterPanel(
                        visible = showFilterPanel,
                        state = state,
                        onUpdateIdentifierFilter = viewModel::updateIdentifierFilter,
                        onUpdateLicenseFilter = viewModel::updateLicenseFilter,
                        onUpdateLicenseSourceFilter = viewModel::updateLicenseSourceFilter,
                        onUpdateResolutionStatusFilter = viewModel::updateResolutionStatusFilter,
                        onUpdateRuleFilter = viewModel::updateRuleFilter,
                        onUpdateSeverityFilter = viewModel::updateSeverityFilter
                    )
                }
            )
        }
    }
}

@Composable
fun ViolationCard(violation: ResolvedRuleViolation) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 8.dp) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                SeverityIcon(violation.severity, resolved = violation.resolutions.isNotEmpty())
                Text(violation.rule)
                violation.pkg?.let { Text(it.toCoordinates()) }
                Box(modifier = Modifier.weight(1f))
                violation.license?.let { Text(it.toString()) }
                Text("Sources: ${violation.licenseSources.joinToString { it.name }}")
            }

            if (violation.resolutions.isNotEmpty()) Divider()

            violation.resolutions.forEach { resolution ->
                Text("Resolved: ${resolution.reason}", fontWeight = FontWeight.Bold)
                ExpandableText(resolution.comment)
                Divider()
            }

            if (violation.message.isNotBlank()) ExpandableText(violation.message)
            if (violation.howToFix.isNotBlank()) ExpandableMarkdown(violation.howToFix)
        }
    }
}

@Composable
@Preview
private fun ViolationCardPreview() {
    val violation = ResolvedRuleViolation(
        pkg = Identifier("Maven:com.example:package:1.0.0-beta"),
        rule = "RULE_NAME",
        license = SpdxLicenseIdExpression("Apache-2.0"),
        licenseSources = setOf(LicenseSource.DECLARED),
        severity = Severity.WARNING,
        message = "Some long message. ".repeat(20),
        howToFix = """
            # HOW TO FIX
            
            * A
            * **Markdown**
            * *String*
        """.trimIndent(),
        resolutions = listOf(
            RuleViolationResolution(
                "",
                RuleViolationResolutionReason.CANT_FIX_EXCEPTION,
                "Some long comment. ".repeat(20)
            )
        )
    )

    Preview {
        ViolationCard(violation)
    }
}

@Composable
fun ViolationsFilterPanel(
    visible: Boolean,
    state: ViolationsState.Success,
    onUpdateIdentifierFilter: (identifier: Identifier?) -> Unit,
    onUpdateLicenseFilter: (license: SpdxSingleLicenseExpression?) -> Unit,
    onUpdateLicenseSourceFilter: (licenseSource: LicenseSource?) -> Unit,
    onUpdateResolutionStatusFilter: (resolutionStatus: ResolutionStatus?) -> Unit,
    onUpdateRuleFilter: (rule: String?) -> Unit,
    onUpdateSeverityFilter: (severity: Severity?) -> Unit
) {
    FilterPanel(visible = visible) {
        FilterButton(
            data = state.filter.severity,
            label = "Severity",
            onFilterChange = onUpdateSeverityFilter,
            convert = { it.name.titlecase() }
        )

        FilterButton(data = state.filter.license, label = "License", onFilterChange = onUpdateLicenseFilter)

        FilterButton(
            data = state.filter.licenseSource,
            label = "License Source",
            onFilterChange = onUpdateLicenseSourceFilter,
            convert = { it.name.titlecase() }
        )

        FilterButton(data = state.filter.rule, label = "Rule", onFilterChange = onUpdateRuleFilter)

        FilterButton(
            data = state.filter.identifier,
            label = "Package",
            onFilterChange = onUpdateIdentifierFilter,
            convert = { it.toCoordinates() }
        )

        FilterButton(
            data = state.filter.resolutionStatus,
            label = "Resolution",
            onFilterChange = onUpdateResolutionStatusFilter,
            convert = { it.name.titlecase() }
        )
    }
}
