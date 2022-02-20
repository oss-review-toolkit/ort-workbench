package org.ossreviewtoolkit.workbench.ui.packages

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
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.licenses.LicenseView
import org.ossreviewtoolkit.utils.common.titlecase
import org.ossreviewtoolkit.utils.spdx.SpdxSingleLicenseExpression
import org.ossreviewtoolkit.workbench.theme.DarkGray
import org.ossreviewtoolkit.workbench.util.FilterButton
import org.ossreviewtoolkit.workbench.util.FilterTextField
import org.ossreviewtoolkit.workbench.util.IconText
import org.ossreviewtoolkit.workbench.util.MaterialIcon
import org.ossreviewtoolkit.workbench.util.enumcase

@Composable
fun Packages(viewModel: PackagesViewModel) {
    val filteredPackages by viewModel.filteredPackages.collectAsState()

    Column(
        modifier = Modifier.padding(15.dp).fillMaxSize()
    ) {
        TitleRow(viewModel)

        PackagesList(filteredPackages)
    }
}

@Composable
private fun TitleRow(viewModel: PackagesViewModel) {
    val filter by viewModel.filter.collectAsState()
    val types by viewModel.types.collectAsState()
    val namespaces by viewModel.namespaces.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val scopes by viewModel.scopes.collectAsState()
    val licenses by viewModel.licenses.collectAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FilterTextField(filter.text) { viewModel.updateFilter(filter.copy(text = it)) }

        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterType(filter.type, types) { viewModel.updateFilter(filter.copy(type = it)) }
                FilterNamespace(filter.namespace, namespaces) { viewModel.updateFilter(filter.copy(namespace = it)) }
                FilterProject(filter.project, projects) { viewModel.updateFilter(filter.copy(project = it)) }
                FilterScope(filter.scope, scopes) { viewModel.updateFilter(filter.copy(scope = it)) }
                FilterLicense(filter.license, licenses) { viewModel.updateFilter(filter.copy(license = it)) }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterIssueStatus(filter.issueStatus) { viewModel.updateFilter(filter.copy(issueStatus = it)) }
                FilterViolationStatus(filter.violationStatus) {
                    viewModel.updateFilter(filter.copy(violationStatus = it))
                }
                FilterVulnerabilityStatus(filter.vulnerabilityStatus) {
                    viewModel.updateFilter(filter.copy(vulnerabilityStatus = it))
                }
                FilterExclusionStatus(filter.exclusionStatus) {
                    viewModel.updateFilter(filter.copy(exclusionStatus = it))
                }
            }
        }
    }
}

@Composable
private fun FilterType(type: String, types: List<String>, onSourceChange: (String) -> Unit) {
    FilterButton(
        selectedItem = type,
        items = listOf("") + types,
        onFilterChange = onSourceChange,
        buttonContent = { if (it.isBlank()) Text("Type") else Text(it) }
    ) { selectedItem ->
        if (selectedItem.isBlank()) Text("All") else Text(selectedItem)
    }
}

@Composable
private fun FilterNamespace(namespace: String, namespaces: List<String>, onSourceChange: (String) -> Unit) {
    FilterButton(
        selectedItem = namespace,
        items = listOf("") + namespaces,
        onFilterChange = onSourceChange,
        buttonContent = { if (it.isBlank()) Text("Namespace") else Text(it) },
        dropdownWidth = 500.dp
    ) { selectedItem ->
        if (selectedItem.isBlank()) Text("All") else Text(selectedItem)
    }
}

@Composable
private fun IdentifierText(identifier: Identifier) {
    Text(identifier.toCoordinates(), maxLines = 1, overflow = TextOverflow.Ellipsis)
}

@Composable
private fun FilterProject(project: Identifier?, projects: List<Identifier>, onSourceChange: (Identifier?) -> Unit) {
    FilterButton(
        selectedItem = project,
        items = listOf(null) + projects,
        onFilterChange = onSourceChange,
        buttonContent = { if (it == null) Text("Project") else IdentifierText(it) },
        buttonWidth = 200.dp,
        dropdownWidth = 500.dp
    ) { selectedItem ->
        if (selectedItem == null) Text("All") else IdentifierText(selectedItem)
    }
}

@Composable
private fun FilterScope(scope: String, scopes: List<String>, onSourceChange: (String) -> Unit) {
    FilterButton(
        selectedItem = scope,
        items = listOf("") + scopes,
        onFilterChange = onSourceChange,
        buttonContent = { if (it.isBlank()) Text("Scope") else Text(it) }
    ) { selectedItem ->
        if (selectedItem.isBlank()) Text("All") else Text(selectedItem)
    }
}

@Composable
private fun FilterLicense(
    license: SpdxSingleLicenseExpression?,
    licenses: List<SpdxSingleLicenseExpression>,
    onLicenseChange: (SpdxSingleLicenseExpression?) -> Unit
) {
    FilterButton(
        selectedItem = license,
        items = listOf(null) + licenses,
        onFilterChange = onLicenseChange,
        buttonContent = { if (it == null) Text("License") else Text(it.toString()) },
        buttonWidth = 200.dp,
        dropdownWidth = 500.dp
    ) {
        if (it == null) Text("All") else Text(it.toString())
    }
}

@Composable
private fun FilterIssueStatus(
    issueStatus: IssueStatus,
    onIssueStatusChange: (IssueStatus) -> Unit
) {
    FilterButton(
        selectedItem = issueStatus,
        items = IssueStatus.values().toList(),
        onFilterChange = onIssueStatusChange,
        buttonContent = { if (it == IssueStatus.ALL) Text("Issues") else Text(it.name.titlecase()) }
    ) {
        Text(it.name.enumcase())
    }
}

@Composable
private fun FilterViolationStatus(
    violationStatus: ViolationStatus,
    onViolationStatusChange: (ViolationStatus) -> Unit
) {
    FilterButton(
        selectedItem = violationStatus,
        items = ViolationStatus.values().toList(),
        onFilterChange = onViolationStatusChange,
        buttonContent = { if (it == ViolationStatus.ALL) Text("Violations") else Text(it.name.titlecase()) },
        dropdownWidth = 200.dp
    ) {
        Text(it.name.enumcase())
    }
}

@Composable
private fun FilterVulnerabilityStatus(
    vulnerabilityStatus: VulnerabilityStatus,
    onVulnerabilityStatusChange: (VulnerabilityStatus) -> Unit
) {
    FilterButton(
        selectedItem = vulnerabilityStatus,
        items = VulnerabilityStatus.values().toList(),
        onFilterChange = onVulnerabilityStatusChange,
        buttonContent = { if (it == VulnerabilityStatus.ALL) Text("Vulnerabilities") else Text(it.name.titlecase()) },
        buttonWidth = 200.dp,
        dropdownWidth = 200.dp
    ) {
        Text(it.name.enumcase())
    }
}

@Composable
private fun FilterExclusionStatus(
    exclusionStatus: ExclusionStatus,
    onExclusionStatusChange: (ExclusionStatus) -> Unit
) {
    FilterButton(
        selectedItem = exclusionStatus,
        items = ExclusionStatus.values().toList(),
        onFilterChange = onExclusionStatusChange,
        buttonContent = { if (it == ExclusionStatus.ALL) Text("Exclusion") else Text(it.name.titlecase()) }
    ) {
        Text(it.name.titlecase())
    }
}

@Composable
fun PackagesList(packages: List<PackageInfo>) {
    if (packages.isEmpty()) {
        Text("No packages found.", modifier = Modifier.padding(top = 15.dp))
    } else {
        Box(modifier = Modifier.fillMaxSize().padding(top = 15.dp)) {
            val listState = rememberLazyListState()

            LazyColumn(
                contentPadding = PaddingValues(vertical = 15.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(packages.size, key = { it }) { index ->
                    PackageCard(packages[index])
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
fun PackageCard(pkg: PackageInfo) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 8.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {

                Text(pkg.pkg.id.toCoordinates(), fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("PURL: ${pkg.pkg.purl}", style = MaterialTheme.typography.caption)
                    if (pkg.pkg.cpe != null) {
                        Text("CPE: ${pkg.pkg.cpe}", style = MaterialTheme.typography.caption)
                    }
                }
            }

            Divider()

            if (pkg.pkg.description.isNotBlank()) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(pkg.pkg.description)
                }

                Divider()
            }

            Column(modifier = Modifier.padding(10.dp)) {

                CompositionLocalProvider(LocalContentColor provides DarkGray) {
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
