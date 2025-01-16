package org.ossreviewtoolkit.workbench.ui.packages

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.licenses.LicenseView
import org.ossreviewtoolkit.utils.common.titlecase
import org.ossreviewtoolkit.utils.spdx.SpdxSingleLicenseExpression
import org.ossreviewtoolkit.workbench.composables.CircularProgressBox
import org.ossreviewtoolkit.workbench.composables.FilterButton
import org.ossreviewtoolkit.workbench.composables.FilterPanel
import org.ossreviewtoolkit.workbench.composables.IconText
import org.ossreviewtoolkit.workbench.composables.ListScreenContent
import org.ossreviewtoolkit.workbench.composables.ListScreenList
import org.ossreviewtoolkit.workbench.composables.Preview

@Composable
fun Packages(viewModel: PackagesViewModel, onSelectPackage: (Identifier) -> Unit) {
    val stateState = viewModel.state.collectAsState()

    when (val state = stateState.value) {
        is PackagesState.Loading -> CircularProgressBox(state.processedPackages, state.totalPackages, "package(s)")

        is PackagesState.Success -> {
            ListScreenContent(
                filterText = state.filter.text,
                onUpdateFilterText = viewModel::updateTextFilter,
                list = {
                    ListScreenList(
                        items = state.packages,
                        itemsEmptyText = "No packages found.",
                        item = { pkg ->
                            PackageCard(pkg, onSelectPackage = { onSelectPackage(pkg.curatedPackage.metadata.id) })
                        }
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
    }
}

@Composable
fun PackageCard(pkg: PackageInfo, onSelectPackage: () -> Unit) {
    PackageCard(
        id = pkg.curatedPackage.metadata.id,
        description = pkg.curatedPackage.metadata.description,
        license = pkg.resolvedLicenseInfo.effectiveLicense(LicenseView.CONCLUDED_OR_DECLARED_AND_DETECTED).toString(),
        issues = pkg.issues.size,
        vulnerabilities = pkg.vulnerabilities.size,
        ruleViolations = pkg.violations.size,
        onSelectPackage = onSelectPackage
    )
}

@Composable
fun PackageCard(
    id: Identifier,
    description: String,
    license: String?,
    issues: Int,
    vulnerabilities: Int,
    ruleViolations: Int,
    onSelectPackage: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 8.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(id.toCoordinates(), fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))

            Divider()

            if (description.isNotBlank()) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(description)
                }

                Divider()
            }

            Column(modifier = Modifier.padding(10.dp)) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(license ?: "Unknown license")

                        Box(modifier = Modifier.weight(1f))

                        // TODO: Add links to other view, pre-filtered by this package.
                        // TODO: Take resolutions into account.
                        IconText(rememberVectorPainter(Icons.Default.BugReport), issues.toString())
                        IconText(rememberVectorPainter(Icons.Default.Gavel), ruleViolations.toString())
                        IconText(rememberVectorPainter(Icons.Default.LockOpen), vulnerabilities.toString())

                        TextButton(
                            onClick = onSelectPackage,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                        ) { Text("Details") }

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
    state: PackagesState.Success,
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
        FilterButton(data = state.filter.type, label = "Type", onFilterChange = onUpdateTypeFilter)

        FilterButton(
            data = state.filter.namespace,
            label = "Namespace",
            onFilterChange = onUpdateNamespaceFilter
        )

        FilterButton(
            data = state.filter.project,
            label = "Project",
            onFilterChange = onUpdateProjectFilter,
            convert = { it.toCoordinates() }
        )

        FilterButton(data = state.filter.scope, label = "Scope", onFilterChange = onUpdateScopeFilter)

        FilterButton(data = state.filter.license, label = "License", onFilterChange = onUpdateLicenseFilter)

        FilterButton(
            data = state.filter.issueStatus,
            label = "Issues",
            onFilterChange = onUpdateIssueStatusFilter,
            convert = { it.name.titlecase() }
        )

        FilterButton(
            data = state.filter.violationStatus,
            label = "Violations",
            onFilterChange = onUpdateViolationStatusFilter,
            convert = { it.name.titlecase() }
        )

        FilterButton(
            data = state.filter.vulnerabilityStatus,
            label = "Vulnerabilities",
            onFilterChange = onUpdateVulnerabilityStatusFilter,
            convert = { it.name.titlecase() }
        )

        FilterButton(
            data = state.filter.exclusionStatus,
            label = "Excluded",
            onFilterChange = onUpdateExclusionStatusFilter,
            convert = { it.name.titlecase() }
        )
    }
}

@Composable
@Preview
private fun PackageCardPreview() {
    Preview {
        PackageCard(
            id = Identifier("Maven:org.ossreviewtoolkit:ort-workbench:1.0.0"),
            description = "A desktop workbench for OSS Review Toolkit result files.",
            license = "Apache-2.0",
            issues = 1,
            vulnerabilities = 2,
            ruleViolations = 3,
            onSelectPackage = {}
        )
    }
}
