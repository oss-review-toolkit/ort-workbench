package org.ossreviewtoolkit.workbench.ui.vulnerabilities

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

import java.net.URI

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.VulnerabilityReference
import org.ossreviewtoolkit.model.config.VulnerabilityResolution
import org.ossreviewtoolkit.model.config.VulnerabilityResolutionReason
import org.ossreviewtoolkit.utils.common.titlecase
import org.ossreviewtoolkit.workbench.model.DecoratedVulnerability
import org.ossreviewtoolkit.workbench.model.FilterData
import org.ossreviewtoolkit.workbench.util.ExpandableText
import org.ossreviewtoolkit.workbench.util.FilterButton
import org.ossreviewtoolkit.workbench.util.FilterTextField
import org.ossreviewtoolkit.workbench.util.Preview
import org.ossreviewtoolkit.workbench.util.ResolutionStatus
import org.ossreviewtoolkit.workbench.util.WebLink

@Composable
fun Vulnerabilities(viewModel: VulnerabilitiesViewModel) {
    val state by viewModel.state.collectAsState()

    Column {
        TitleRow(
            state = state,
            onUpdateTextFilter = viewModel::updateTextFilter,
            onUpdateAdvisorsFilter = viewModel::updateAdvisorsFilter,
            onUpdateIdentifiersFilter = viewModel::updateIdentifiersFilter,
            onUpdateResolutionStatusFilter = viewModel::updateResolutionStatusFilter,
            onUpdateScoringSystemsFilter = viewModel::updateScoringSystemsFilter,
            onUpdateSeveritiesFilter = viewModel::updateSeveritiesFilter
        )

        VulnerabilitiesList(state.vulnerabilities)
    }
}

@Composable
private fun TitleRow(
    state: VulnerabilitiesState,
    onUpdateTextFilter: (text: String) -> Unit,
    onUpdateAdvisorsFilter: (advisor: String?) -> Unit,
    onUpdateIdentifiersFilter: (identifier: Identifier?) -> Unit,
    onUpdateResolutionStatusFilter: (status: ResolutionStatus) -> Unit,
    onUpdateScoringSystemsFilter: (scoringSystem: String?) -> Unit,
    onUpdateSeveritiesFilter: (severity: String?) -> Unit,
) {
    TopAppBar(
        modifier = Modifier.zIndex(1f),
        backgroundColor = MaterialTheme.colors.primary,
        title = {},
        actions = {
            Row(modifier = Modifier.padding(vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterTextField(state.textFilter, onFilterChange = onUpdateTextFilter)
                FilterAdvisor(state.advisorFilter, onUpdateAdvisorsFilter)
                FilterScoringSystem(state.scoringSystemFilter, onUpdateScoringSystemsFilter)
                FilterSeverity(state.severityFilter, onUpdateSeveritiesFilter)
                FilterIdentifier(state.identifierFilter, onUpdateIdentifiersFilter)
                FilterResolutionStatus(state.resolutionStatusFilter, onUpdateResolutionStatusFilter)
            }
        }
    )
}

@Composable
private fun FilterAdvisor(filter: FilterData<String?>, onAdvisorChange: (String?) -> Unit) {
    FilterButton(
        selectedItem = filter.selectedItem,
        items = filter.options,
        onFilterChange = onAdvisorChange,
        buttonContent = { if (it == null) Text("Advisor") else Text(it) }
    ) { item ->
        if (item == null) Text("All") else Text(item)
    }
}

@Composable
private fun FilterScoringSystem(filter: FilterData<String?>, onScoringSystemChange: (String?) -> Unit) {
    FilterButton(
        selectedItem = filter.selectedItem,
        items = filter.options,
        onFilterChange = onScoringSystemChange,
        buttonContent = { if (it == null) Text("Scoring System") else Text(it) },
        buttonWidth = 150.dp,
        dropdownWidth = 150.dp
    ) { item ->
        if (item == null) Text("All") else Text(item)
    }
}

@Composable
private fun FilterSeverity(filter: FilterData<String?>, onSeverityChange: (String?) -> Unit) {
    FilterButton(
        selectedItem = filter.selectedItem,
        items = filter.options,
        onFilterChange = onSeverityChange,
        buttonContent = { if (it == null) Text("Severity") else Text(it) }
    ) { item ->
        if (item == null) Text("All") else Text(item)
    }
}

@Composable
private fun IdentifierText(identifier: Identifier) {
    Text(identifier.toCoordinates(), maxLines = 1, overflow = TextOverflow.Ellipsis)
}

@Composable
private fun FilterIdentifier(filter: FilterData<Identifier?>, onIdentifierChange: (Identifier?) -> Unit) {
    FilterButton(
        selectedItem = filter.selectedItem,
        items = filter.options,
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
    filter: FilterData<ResolutionStatus>,
    onResolutionStatusChange: (ResolutionStatus) -> Unit
) {
    FilterButton(
        selectedItem = filter.selectedItem,
        items = filter.options,
        onFilterChange = onResolutionStatusChange,
        buttonContent = { if (it == ResolutionStatus.ALL) Text("Resolution") else Text(it.name.titlecase()) }
    ) { item ->
        Text(item.name.titlecase())
    }
}

@Composable
fun VulnerabilitiesList(vulnerabilities: List<DecoratedVulnerability>) {
    if (vulnerabilities.isEmpty()) {
        Text("No vulnerabilities found.", modifier = Modifier.padding(15.dp))
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            val listState = rememberLazyListState()

            LazyColumn(
                contentPadding = PaddingValues(15.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                state = listState
            ) {
                items(vulnerabilities.size, key = { it }) { index ->
                    VulnerabilityCard(vulnerabilities[index])
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
fun VulnerabilityCard(vulnerability: DecoratedVulnerability) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 8.dp) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(vulnerability.id, fontWeight = FontWeight.Bold)
                Text(vulnerability.pkg.toCoordinates())
                Box(modifier = Modifier.weight(1f))
                Text("Source: ${vulnerability.advisor}")
            }

            if (vulnerability.resolutions.isNotEmpty()) Divider()

            vulnerability.resolutions.forEach { resolution ->
                Text("Resolved: ${resolution.reason}", fontWeight = FontWeight.Bold)
                ExpandableText(resolution.comment)
                Divider()
            }

            vulnerability.references.forEach { reference ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    reference.scoringSystem?.let { Text("$it:") }
                    reference.severity?.let { Text(it) }
                    Box(modifier = Modifier.weight(1f))
                    Text(reference.url.host.orEmpty())
                    WebLink("Link", reference.url.toString())
                }
            }
        }
    }
}

@Composable
@Preview
private fun VulnerabilityCardPreview() {
    val vulnerability = DecoratedVulnerability(
        pkg = Identifier("Maven:com.example:package:1.0.0-beta"),
        resolutions = listOf(
            VulnerabilityResolution(
                "v-id",
                VulnerabilityResolutionReason.CANT_FIX_VULNERABILITY,
                "Some long comment. ".repeat(20)
            )
        ),
        advisor = "Advisor",
        id = "v-id",
        references = listOf(
            VulnerabilityReference(
                URI("http://example.com"),
                scoringSystem = "scosy",
                severity = "severe"
            )
        )
    )

    Preview {
        VulnerabilityCard(vulnerability)
    }
}
