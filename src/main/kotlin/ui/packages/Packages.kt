package org.ossreviewtoolkit.workbench.ui.packages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.licenses.LicenseView
import org.ossreviewtoolkit.utils.common.titlecase
import org.ossreviewtoolkit.utils.spdx.SpdxSingleLicenseExpression
import org.ossreviewtoolkit.workbench.composables.FilterButton
import org.ossreviewtoolkit.workbench.composables.FilterPanel
import org.ossreviewtoolkit.workbench.composables.IconText
import org.ossreviewtoolkit.workbench.composables.ListScreenContent
import org.ossreviewtoolkit.workbench.composables.ListScreenList
import org.ossreviewtoolkit.workbench.utils.MaterialIcon

@Composable
fun Packages(viewModel: PackagesViewModel) {
    val state by viewModel.state.collectAsState()

    ListScreenContent(
        filterText = state.textFilter,
        onUpdateFilterText = viewModel::updateTextFilter,
        list = {
            ListScreenList(
                items = state.packages,
                itemsEmptyText = "No packages found.",
                item = { PackageCard(it) }
            )
        },
        filterPanel = { showFilterPanel ->
            PackagesFilterPanel(
                visible = showFilterPanel,
                state = state,
                onUpdateExclusionStatusFilter = viewModel::updateExclusionStatusFilter,
                onUpdateIssueStatusFilter = viewModel::updateIssueStatusFilter,
                onUpdateLicenseFilter = viewModel::updateLicenseFilter,
                onUpdateNamespaceFilter = viewModel::updateNamespaceFilter,
                onUpdateProjectFilter = viewModel::updateProjectFilter,
                onUpdateScopeFilter = viewModel::updateScopeFilter,
                onUpdateTypeFilter = viewModel::updateTypeFilter,
                onUpdateViolationStatusFilter = viewModel::updateViolationStatusFilter,
                onUpdateVulnerabilityStatusFilter = viewModel::updateVulnerabilityStatusFilter
            )
        }
    )
}

@Composable
fun PackageCard(pkg: PackageInfo) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 8.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(pkg.metadata.id.toCoordinates(), fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("PURL: ${pkg.metadata.purl}", style = MaterialTheme.typography.caption)
                    if (pkg.metadata.cpe != null) {
                        Text("CPE: ${pkg.metadata.cpe}", style = MaterialTheme.typography.caption)
                    }
                }
            }

            Divider()

            if (pkg.metadata.description.isNotBlank()) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(pkg.metadata.description)
                }

                Divider()
            }

            Column(modifier = Modifier.padding(10.dp)) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        val license =
                            pkg.resolvedLicenseInfo.effectiveLicense(LicenseView.CONCLUDED_OR_DECLARED_AND_DETECTED)

                        Text("${license ?: "Unknown license"}")

                        Box(modifier = Modifier.weight(1f))

                        // TODO: Add links to other view, pre-filtered by this package.
                        // TODO: Take resolutions into account.
                        IconText(painterResource(MaterialIcon.BUG_REPORT.resource), pkg.issues.size.toString())
                        IconText(painterResource(MaterialIcon.GAVEL.resource), pkg.violations.size.toString())
                        IconText(painterResource(MaterialIcon.LOCK_OPEN.resource), pkg.vulnerabilities.size.toString())

                        // TODO: Add entries for scan results and curations.
                    }
                }

                // TODO: Add link to package details page.
            }
        }
    }
}

@Composable
fun PackagesFilterPanel(
    visible: Boolean,
    state: PackagesState,
    onUpdateExclusionStatusFilter: (exclusionStatus: ExclusionStatus?) -> Unit,
    onUpdateIssueStatusFilter: (issueStatus: IssueStatus?) -> Unit,
    onUpdateLicenseFilter: (license: SpdxSingleLicenseExpression?) -> Unit,
    onUpdateNamespaceFilter: (namespace: String?) -> Unit,
    onUpdateProjectFilter: (project: Identifier?) -> Unit,
    onUpdateScopeFilter: (scope: String?) -> Unit,
    onUpdateTypeFilter: (type: String?) -> Unit,
    onUpdateViolationStatusFilter: (violationStatus: ViolationStatus?) -> Unit,
    onUpdateVulnerabilityStatusFilter: (vulnerabilityStatus: VulnerabilityStatus?) -> Unit
) {
    FilterPanel(visible = visible) {
        FilterButton(data = state.typeFilter, label = "Type", onFilterChange = onUpdateTypeFilter)

        FilterButton(
            data = state.namespaceFilter,
            label = "Namespace",
            onFilterChange = onUpdateNamespaceFilter
        )

        FilterButton(
            data = state.projectFilter,
            label = "Project",
            onFilterChange = onUpdateProjectFilter,
            convert = { it.toCoordinates() }
        )

        FilterButton(data = state.scopeFilter, label = "Scope", onFilterChange = onUpdateScopeFilter)

        FilterButton(data = state.licenseFilter, label = "License", onFilterChange = onUpdateLicenseFilter)

        FilterButton(
            data = state.issueStatusFilter,
            label = "Issues",
            onFilterChange = onUpdateIssueStatusFilter,
            convert = { it.name.titlecase() }
        )

        FilterButton(
            data = state.violationStatusFilter,
            label = "Violations",
            onFilterChange = onUpdateViolationStatusFilter,
            convert = { it.name.titlecase() }
        )

        FilterButton(
            data = state.vulnerabilityStatusFilter,
            label = "Vulnerabilities",
            onFilterChange = onUpdateVulnerabilityStatusFilter,
            convert = { it.name.titlecase() }
        )

        FilterButton(
            data = state.exclusionStatusFilter,
            label = "Excluded",
            onFilterChange = onUpdateExclusionStatusFilter,
            convert = { it.name.titlecase() }
        )
    }
}
