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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

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
    val filteredViolations by viewModel.filteredViolations.collectAsState()

    Column(
        modifier = Modifier.padding(15.dp).fillMaxSize()
    ) {
        TitleRow(viewModel)

        ViolationsList(filteredViolations)
    }
}

@Composable
private fun TitleRow(viewModel: ViolationsViewModel) {
    val filter by viewModel.filter.collectAsState()
    val identifiers by viewModel.identifiers.collectAsState()
    val licenses by viewModel.licenses.collectAsState()
    val rules by viewModel.rules.collectAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FilterTextField(filter.text) { viewModel.updateFilter(filter.copy(text = it)) }
        FilterSeverity(filter.severity) { viewModel.updateFilter(filter.copy(severity = it)) }
        FilterLicense(filter.license, licenses) { viewModel.updateFilter(filter.copy(license = it)) }
        FilterLicenseSource(filter.licenseSource) { viewModel.updateFilter(filter.copy(licenseSource = it)) }
        FilterRule(filter.rule, rules) { viewModel.updateFilter(filter.copy(rule = it)) }
        FilterIdentifier(filter.identifier, identifiers) { viewModel.updateFilter(filter.copy(identifier = it)) }
        FilterResolutionStatus(filter.resolutionStatus) { viewModel.updateFilter(filter.copy(resolutionStatus = it)) }
    }
}

@Composable
private fun SeverityItem(severity: Severity) {
    SeverityIcon(severity)
    Text(severity.name.titlecase(), modifier = Modifier.padding(start = 5.dp))
}

@Composable
private fun FilterSeverity(severity: Severity?, onSeverityChange: (Severity?) -> Unit) {
    FilterButton(
        selectedItem = severity,
        items = listOf(null, Severity.HINT, Severity.WARNING, Severity.ERROR),
        onFilterChange = onSeverityChange,
        buttonContent = { if (it == null) Text("Severity") else SeverityItem(it) }
    ) { item ->
        when (item) {
            null -> Text("All")
            else -> SeverityItem(item)
        }
    }
}

@Composable
private fun FilterLicense(
    license: String,
    licenses: List<SpdxSingleLicenseExpression>,
    onLicenseChange: (String) -> Unit
) {
    FilterButton(
        selectedItem = license,
        items = listOf("") + licenses.map { it.toString() },
        onFilterChange = onLicenseChange,
        buttonContent = { if (it.isEmpty()) Text("License") else Text(it) }
    ) { item ->
        if (item.isEmpty()) Text("All") else Text(item)
    }
}

@Composable
private fun FilterLicenseSource(
    licenseSource: LicenseSource?,
    onLicenseSourceChange: (LicenseSource?) -> Unit
) {
    FilterButton(
        selectedItem = licenseSource,
        items = listOf(null, LicenseSource.DECLARED, LicenseSource.DETECTED, LicenseSource.CONCLUDED),
        onFilterChange = onLicenseSourceChange,
        buttonContent = { if (it == null) Text("License Source") else Text(it.name.titlecase()) },
        buttonWidth = 150.dp,
        dropdownWidth = 150.dp
    ) { item ->
        if (item == null) Text("All") else Text(item.name.titlecase())
    }
}

@Composable
private fun FilterRule(
    rule: String,
    rules: List<String>,
    onRuleChange: (String) -> Unit
) {
    FilterButton(
        selectedItem = rule,
        items = listOf("") + rules,
        onFilterChange = onRuleChange,
        buttonContent = { if (it.isEmpty()) Text("Rule") else Text(it) }
    ) { item ->
        if (item.isEmpty()) Text("All") else Text(item)
    }
}

@Composable
private fun IdentifierText(identifier: Identifier) {
    Text(identifier.toCoordinates(), maxLines = 1, overflow = TextOverflow.Ellipsis)
}

@Composable
private fun FilterIdentifier(
    identifier: Identifier?,
    identifiers: List<Identifier>,
    onIdentifierChange: (Identifier?) -> Unit
) {
    FilterButton(
        selectedItem = identifier,
        items = listOf(null) + identifiers,
        onFilterChange = onIdentifierChange,
        buttonContent = { if (it == null) Text("Package") else IdentifierText(it) },
        buttonWidth = 200.dp,
        dropdownWidth = 500.dp
    ) { item ->
        if (item == null) Text("All") else IdentifierText(item)
    }
}

@Composable
private fun FilterResolutionStatus(
    resolutionStatus: ResolutionStatus,
    onResolutionStatusChange: (ResolutionStatus) -> Unit
) {
    FilterButton(
        selectedItem = resolutionStatus,
        items = listOf(ResolutionStatus.ALL, ResolutionStatus.RESOLVED, ResolutionStatus.UNRESOLVED),
        onFilterChange = onResolutionStatusChange,
        buttonContent = { if (it == ResolutionStatus.ALL) Text("Resolution") else Text(it.name.titlecase()) }
    ) { item ->
        Text(item.name.titlecase())
    }
}

@Composable
fun ViolationsList(violations: List<Violation>) {
    if (violations.isEmpty()) {
        Text("No violations found.", modifier = Modifier.padding(top = 15.dp))
    } else {
        Box(modifier = Modifier.fillMaxSize().padding(top = 15.dp)) {
            val listState = rememberLazyListState()

            LazyColumn(
                contentPadding = PaddingValues(vertical = 15.dp),
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
