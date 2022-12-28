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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

import java.net.URI

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.VulnerabilityReference
import org.ossreviewtoolkit.model.config.VulnerabilityResolution
import org.ossreviewtoolkit.model.config.VulnerabilityResolutionReason
import org.ossreviewtoolkit.utils.common.titlecase
import org.ossreviewtoolkit.workbench.composables.ExpandableText
import org.ossreviewtoolkit.workbench.composables.FilterButton
import org.ossreviewtoolkit.workbench.composables.FilterTextField
import org.ossreviewtoolkit.workbench.composables.Preview
import org.ossreviewtoolkit.workbench.composables.SidePanel
import org.ossreviewtoolkit.workbench.composables.WebLink
import org.ossreviewtoolkit.workbench.model.DecoratedVulnerability
import org.ossreviewtoolkit.workbench.model.ResolutionStatus
import org.ossreviewtoolkit.workbench.utils.MaterialIcon

@Composable
fun Vulnerabilities(viewModel: VulnerabilitiesViewModel) {
    val state by viewModel.state.collectAsState()

    var showFilterPanel by remember { mutableStateOf(false) }

    Column {
        TitleRow(
            state = state,
            onUpdateTextFilter = viewModel::updateTextFilter,
            onToggleFilter = { showFilterPanel = !showFilterPanel }
        )

        Row {
            Box(modifier = Modifier.weight(1f)) {
                VulnerabilitiesList(state.vulnerabilities)
            }

            SidePanel(visible = showFilterPanel) {
                VulnerabilitiesFilterPanel(
                    state = state,
                    onUpdateAdvisorsFilter = viewModel::updateAdvisorsFilter,
                    onUpdateIdentifiersFilter = viewModel::updateIdentifiersFilter,
                    onUpdateResolutionStatusFilter = viewModel::updateResolutionStatusFilter,
                    onUpdateScoringSystemsFilter = viewModel::updateScoringSystemsFilter,
                    onUpdateSeveritiesFilter = viewModel::updateSeveritiesFilter
                )
            }
        }
    }
}

@Composable
private fun TitleRow(
    state: VulnerabilitiesState,
    onUpdateTextFilter: (text: String) -> Unit,
    onToggleFilter: () -> Unit
) {
    TopAppBar(
        modifier = Modifier.zIndex(1f),
        backgroundColor = MaterialTheme.colors.primary,
        title = {},
        actions = {
            Row(modifier = Modifier.padding(vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterTextField(state.textFilter, onFilterChange = onUpdateTextFilter)

                IconButton(onClick = onToggleFilter) {
                    Icon(
                        painterResource(MaterialIcon.FILTER_LIST.resource),
                        contentDescription = "Filter",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    )
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

@Composable
fun VulnerabilitiesFilterPanel(
    state: VulnerabilitiesState,
    onUpdateAdvisorsFilter: (advisor: String?) -> Unit,
    onUpdateIdentifiersFilter: (identifier: Identifier?) -> Unit,
    onUpdateResolutionStatusFilter: (status: ResolutionStatus?) -> Unit,
    onUpdateScoringSystemsFilter: (scoringSystem: String?) -> Unit,
    onUpdateSeveritiesFilter: (severity: String?) -> Unit,
) {
    Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Filters", style = MaterialTheme.typography.h4)

        FilterButton(data = state.advisorFilter, label = "Advisor", onFilterChange = onUpdateAdvisorsFilter)

        FilterButton(
            data = state.scoringSystemFilter,
            label = "Scoring System",
            onFilterChange = onUpdateScoringSystemsFilter
        )

        FilterButton(
            data = state.severityFilter,
            label = "Severity",
            onFilterChange = onUpdateSeveritiesFilter
        )

        FilterButton(
            data = state.identifierFilter,
            label = "Package",
            onFilterChange = onUpdateIdentifiersFilter,
            convert = { it.toCoordinates() }
        )

        FilterButton(
            data = state.resolutionStatusFilter,
            label = "Resolution",
            onFilterChange = onUpdateResolutionStatusFilter,
            convert = { it.name.titlecase() }
        )
    }
}

@Composable
@Preview
fun VulnerabilitiesFilterPanelPreview() {
    Preview {
        VulnerabilitiesFilterPanel(
            state = VulnerabilitiesState.INITIAL,
            onUpdateAdvisorsFilter = {},
            onUpdateIdentifiersFilter = {},
            onUpdateResolutionStatusFilter = {},
            onUpdateScoringSystemsFilter = {},
            onUpdateSeveritiesFilter = {}
        )
    }
}
