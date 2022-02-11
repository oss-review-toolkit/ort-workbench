package org.ossreviewtoolkit.workbench.ui.issues

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import java.time.Instant

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.model.config.IssueResolution
import org.ossreviewtoolkit.model.config.IssueResolutionReason
import org.ossreviewtoolkit.utils.common.titlecase
import org.ossreviewtoolkit.workbench.util.ExpandableText
import org.ossreviewtoolkit.workbench.util.FilterButton
import org.ossreviewtoolkit.workbench.util.FilterTextField
import org.ossreviewtoolkit.workbench.util.Preview
import org.ossreviewtoolkit.workbench.util.ResolutionStatus
import org.ossreviewtoolkit.workbench.util.SeverityIcon

@Composable
fun Issues(viewModel: IssuesViewModel) {
    val filteredIssues by viewModel.filteredIssues.collectAsState()

    Column(
        modifier = Modifier.padding(15.dp).fillMaxSize()
    ) {
        TitleRow(viewModel)

        IssuesList(filteredIssues)
    }
}

@Composable
private fun TitleRow(viewModel: IssuesViewModel) {
    val filter by viewModel.filter.collectAsState()
    val identifiers by viewModel.identifiers.collectAsState()
    val sources by viewModel.sources.collectAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FilterTextField(filter.text) { viewModel.updateFilter(filter.copy(text = it)) }
        FilterSeverity(filter.severity) { viewModel.updateFilter(filter.copy(severity = it)) }
        FilterSource(filter.source, sources) { viewModel.updateFilter(filter.copy(source = it)) }
        FilterTool(filter.tool) { viewModel.updateFilter(filter.copy(tool = it)) }
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
    ) { selectedItem ->
        when (selectedItem) {
            null -> Text("All")
            else -> SeverityItem(selectedItem)
        }
    }
}

@Composable
private fun FilterSource(source: String, sources: List<String>, onSourceChange: (String) -> Unit) {
    FilterButton(
        selectedItem = source,
        items = listOf("") + sources,
        onFilterChange = onSourceChange,
        buttonContent = { if (it.isBlank()) Text("Source") else Text(it) }
    ) { selectedItem ->
        if (selectedItem.isBlank()) Text("All") else Text(selectedItem)
    }
}

@Composable
private fun FilterTool(tool: Tool?, onToolChange: (Tool?) -> Unit) {
    FilterButton(
        selectedItem = tool,
        items = listOf(null, Tool.ANALYZER, Tool.ADVISOR, Tool.SCANNER),
        onFilterChange = onToolChange,
        buttonContent = { if (it == null) Text("Tool") else Text(it.name.titlecase()) }
    ) { selectedItem ->
        if (selectedItem == null) Text("All") else Text(selectedItem.name.titlecase())
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
    ) {
        if (it == null) Text("All") else IdentifierText(it)
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
    ) {
        Text(it.name.titlecase())
    }
}

@Composable
fun IssuesList(issues: List<Issue>) {
    if (issues.isEmpty()) {
        Text("No issues found.", modifier = Modifier.padding(top = 15.dp))
    } else {
        Box(modifier = Modifier.fillMaxSize().padding(top = 15.dp)) {
            val listState = rememberLazyListState()

            LazyColumn(
                contentPadding = PaddingValues(vertical = 15.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(issues.size, key = { it }) { index ->
                    IssueCard(issues[index])
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
fun IssueCard(issue: Issue) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 8.dp) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                SeverityIcon(issue.severity, resolved = issue.resolutions.isNotEmpty())
                Text(issue.id.toCoordinates())
                Box(modifier = Modifier.weight(1f))
                Text("Source: ${issue.source}")
            }

            if (issue.resolutions.isNotEmpty()) Divider()

            issue.resolutions.forEach { resolution ->
                Text("Resolved: ${resolution.reason}", fontWeight = FontWeight.Bold)
                ExpandableText(resolution.comment)
                Divider()
            }

            ExpandableText(issue.message, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
@Preview
private fun IssueCardPreview() {
    val issue = Issue(
        id = Identifier("Maven:org.example:package:1.0.0-beta"),
        tool = Tool.ANALYZER,
        resolutions = listOf(
            IssueResolution("", IssueResolutionReason.BUILD_TOOL_ISSUE, "Some long explanation. ".repeat(20)),
            IssueResolution("", IssueResolutionReason.BUILD_TOOL_ISSUE, "Some long explanation. ".repeat(20))
        ),
        timestamp = Instant.now(),
        source = "Maven",
        message = "Some long error message. ".repeat(20),
        severity = Severity.WARNING
    )

    Preview {
        IssueCard(issue)
    }
}
