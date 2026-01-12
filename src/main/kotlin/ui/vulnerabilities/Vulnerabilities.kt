package org.ossreviewtoolkit.workbench.ui.vulnerabilities

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import java.net.URI

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.config.VulnerabilityResolution
import org.ossreviewtoolkit.model.config.VulnerabilityResolutionReason
import org.ossreviewtoolkit.model.vulnerabilities.VulnerabilityReference
import org.ossreviewtoolkit.utils.common.titlecase
import org.ossreviewtoolkit.workbench.composables.CircularProgressBox
import org.ossreviewtoolkit.workbench.composables.ExpandableText
import org.ossreviewtoolkit.workbench.composables.FilterButton
import org.ossreviewtoolkit.workbench.composables.FilterPanel
import org.ossreviewtoolkit.workbench.composables.ListScreenContent
import org.ossreviewtoolkit.workbench.composables.ListScreenList
import org.ossreviewtoolkit.workbench.composables.Preview
import org.ossreviewtoolkit.workbench.composables.WebLink
import org.ossreviewtoolkit.workbench.model.ResolutionStatus
import org.ossreviewtoolkit.workbench.model.ResolvedVulnerability

@Composable
fun Vulnerabilities(viewModel: VulnerabilitiesViewModel) {
    val stateState = viewModel.state.collectAsState()

    when (val state = stateState.value) {
        is VulnerabilitiesState.Loading -> CircularProgressBox()

        is VulnerabilitiesState.Success -> {
            ListScreenContent(
                filterText = state.filter.text,
                onUpdateFilterText = viewModel::updateTextFilter,
                list = {
                    ListScreenList(
                        items = state.vulnerabilities,
                        itemsEmptyText = "No vulnerabilities found.",
                        item = { VulnerabilityCard(it) }
                    )
                },
                filterPanel = { showFilterPanel ->
                    VulnerabilitiesFilterPanel(
                        visible = showFilterPanel,
                        state = state,
                        onUpdateAdvisorsFilter = viewModel::updateAdvisorsFilter,
                        onUpdateIdentifiersFilter = viewModel::updateIdentifiersFilter,
                        onUpdateResolutionStatusFilter = viewModel::updateResolutionStatusFilter,
                        onUpdateScoringSystemsFilter = viewModel::updateScoringSystemsFilter,
                        onUpdateSeveritiesFilter = viewModel::updateSeveritiesFilter
                    )
                }
            )
        }
    }
}

@Composable
fun VulnerabilityCard(vulnerability: ResolvedVulnerability) {
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
    val vulnerability = ResolvedVulnerability(
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
                severity = "severe",
                score = 42.0f,
                vector = "vector"
            )
        )
    )

    Preview {
        VulnerabilityCard(vulnerability)
    }
}

@Composable
fun VulnerabilitiesFilterPanel(
    visible: Boolean,
    state: VulnerabilitiesState.Success,
    onUpdateAdvisorsFilter: (advisor: String?) -> Unit,
    onUpdateIdentifiersFilter: (identifier: Identifier?) -> Unit,
    onUpdateResolutionStatusFilter: (status: ResolutionStatus?) -> Unit,
    onUpdateScoringSystemsFilter: (scoringSystem: String?) -> Unit,
    onUpdateSeveritiesFilter: (severity: String?) -> Unit,
) {
    FilterPanel(visible = visible) {
        FilterButton(data = state.filter.advisor, label = "Advisor", onFilterChange = onUpdateAdvisorsFilter)

        FilterButton(
            data = state.filter.scoringSystem,
            label = "Scoring System",
            onFilterChange = onUpdateScoringSystemsFilter
        )

        FilterButton(
            data = state.filter.severity,
            label = "Severity",
            onFilterChange = onUpdateSeveritiesFilter
        )

        FilterButton(
            data = state.filter.identifier,
            label = "Package",
            onFilterChange = onUpdateIdentifiersFilter,
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

@Composable
@Preview
fun VulnerabilitiesFilterPanelPreview() {
    Preview {
        VulnerabilitiesFilterPanel(
            visible = true,
            state = VulnerabilitiesState.Success(emptyList(), VulnerabilitiesFilter()),
            onUpdateAdvisorsFilter = {},
            onUpdateIdentifiersFilter = {},
            onUpdateResolutionStatusFilter = {},
            onUpdateScoringSystemsFilter = {},
            onUpdateSeveritiesFilter = {}
        )
    }
}
