package org.ossreviewtoolkit.workbench.ui.violations

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.LicenseSource
import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.model.config.RuleViolationResolution
import org.ossreviewtoolkit.model.config.RuleViolationResolutionReason
import org.ossreviewtoolkit.utils.common.titlecase
import org.ossreviewtoolkit.utils.spdx.SpdxLicenseIdExpression
import org.ossreviewtoolkit.utils.spdx.SpdxSingleLicenseExpression
import org.ossreviewtoolkit.workbench.model.Violation
import org.ossreviewtoolkit.workbench.util.ExpandableMarkdown
import org.ossreviewtoolkit.workbench.util.ExpandableText
import org.ossreviewtoolkit.workbench.util.FilterButton
import org.ossreviewtoolkit.workbench.util.FilterTextField
import org.ossreviewtoolkit.workbench.util.Preview
import org.ossreviewtoolkit.workbench.util.ResolutionStatus
import org.ossreviewtoolkit.workbench.util.SeverityIcon

@Composable
@Preview
fun Violations(viewModel: ViolationsViewModel) {
    val state by viewModel.state.collectAsState()

    Column {
        TitleRow(
            state = state,
            onUpdateTextFilter = viewModel::updateTextFilter,
            onUpdateIdentifierFilter = viewModel::updateIdentifierFilter,
            onUpdateLicenseFilter = viewModel::updateLicenseFilter,
            onUpdateLicenseSourceFilter = viewModel::updateLicenseSourceFilter,
            onUpdateResolutionStatusFilter = viewModel::updateResolutionStatusFilter,
            onUpdateRuleFilter = viewModel::updateRuleFilter,
            onUpdateSeverityFilter = viewModel::updateSeverityFilter
        )

        ViolationsList(state.violations)
    }
}

@Composable
private fun TitleRow(
    state: ViolationsState,
    onUpdateTextFilter: (text: String) -> Unit,
    onUpdateIdentifierFilter: (identifier: Identifier?) -> Unit,
    onUpdateLicenseFilter: (license: SpdxSingleLicenseExpression?) -> Unit,
    onUpdateLicenseSourceFilter: (licenseSource: LicenseSource?) -> Unit,
    onUpdateResolutionStatusFilter: (resolutionStatus: ResolutionStatus?) -> Unit,
    onUpdateRuleFilter: (rule: String?) -> Unit,
    onUpdateSeverityFilter: (severity: Severity?) -> Unit
) {
    TopAppBar(
        modifier = Modifier.zIndex(1f),
        backgroundColor = MaterialTheme.colors.primary,
        title = {},
        actions = {
            Row(modifier = Modifier.padding(vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterTextField(state.textFilter, onFilterChange = onUpdateTextFilter)

                FilterButton(
                    data = state.severityFilter,
                    label = "Severity",
                    onFilterChange = onUpdateSeverityFilter,
                    convert = { it?.name?.titlecase() ?: "" }
                )

                FilterButton(data = state.licenseFilter, label = "License", onFilterChange = onUpdateLicenseFilter)

                FilterButton(
                    data = state.licenseSourceFilter,
                    label = "License Source",
                    onFilterChange = onUpdateLicenseSourceFilter,
                    convert = { it?.name?.titlecase() ?: "" }
                )

                FilterButton(data = state.ruleFilter, label = "Rule", onFilterChange = onUpdateRuleFilter)

                FilterButton(
                    data = state.identifierFilter,
                    label = "Package",
                    onFilterChange = onUpdateIdentifierFilter,
                    convert = { it?.toCoordinates() ?: "" }
                )

                FilterButton(
                    data = state.resolutionStatusFilter,
                    label = "Resolution",
                    onFilterChange = onUpdateResolutionStatusFilter,
                    convert = { it?.name?.titlecase() ?: "" }
                )
            }
        }
    )
}

@Composable
fun ViolationsList(violations: List<Violation>) {
    if (violations.isEmpty()) {
        Text("No violations found.", modifier = Modifier.padding(15.dp))
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            val listState = rememberLazyListState()

            LazyColumn(
                contentPadding = PaddingValues(15.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                state = listState
            ) {
                items(violations.size, key = { it }) { index ->
                    ViolationCard(violations[index])
                }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState = listState)
            )
        }
    }
}

@Composable
fun ViolationCard(violation: Violation) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 8.dp) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                SeverityIcon(violation.severity, resolved = violation.resolutions.isNotEmpty())
                Text(violation.rule)
                violation.pkg?.let { Text(it.toCoordinates()) }
                Box(modifier = Modifier.weight(1f))
                violation.license?.let { Text(it.toString()) }
                violation.licenseSource?.let { Text("Source: ${it.name}") }
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
    val violation = Violation(
        pkg = Identifier("Maven:com.example:package:1.0.0-beta"),
        rule = "RULE_NAME",
        license = SpdxLicenseIdExpression("Apache-2.0"),
        licenseSource = LicenseSource.DECLARED,
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
