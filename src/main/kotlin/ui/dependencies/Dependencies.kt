@file:OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)

package org.ossreviewtoolkit.workbench.ui.dependencies

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

import org.ossreviewtoolkit.model.CuratedPackage
import org.ossreviewtoolkit.model.Hash
import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.OrtIssue
import org.ossreviewtoolkit.model.Package
import org.ossreviewtoolkit.model.PackageCurationResult
import org.ossreviewtoolkit.model.PackageLinkage
import org.ossreviewtoolkit.model.Project
import org.ossreviewtoolkit.model.RemoteArtifact
import org.ossreviewtoolkit.model.VcsInfo
import org.ossreviewtoolkit.model.VcsType
import org.ossreviewtoolkit.model.licenses.ConcludedLicenseInfo
import org.ossreviewtoolkit.model.licenses.DeclaredLicenseInfo
import org.ossreviewtoolkit.model.licenses.DetectedLicenseInfo
import org.ossreviewtoolkit.model.licenses.LicenseInfo
import org.ossreviewtoolkit.model.licenses.LicenseView
import org.ossreviewtoolkit.model.licenses.ResolvedLicenseInfo
import org.ossreviewtoolkit.utils.ort.ProcessedDeclaredLicense
import org.ossreviewtoolkit.utils.spdx.SpdxExpression
import org.ossreviewtoolkit.utils.spdx.SpdxLicense
import org.ossreviewtoolkit.utils.spdx.toExpression
import org.ossreviewtoolkit.workbench.util.CaptionedColumn
import org.ossreviewtoolkit.workbench.util.CaptionedText
import org.ossreviewtoolkit.workbench.util.ErrorCard
import org.ossreviewtoolkit.workbench.util.Expandable
import org.ossreviewtoolkit.workbench.util.ExpandableText
import org.ossreviewtoolkit.workbench.util.FilterTextField
import org.ossreviewtoolkit.workbench.util.MaterialIcon
import org.ossreviewtoolkit.workbench.util.Preview
import org.ossreviewtoolkit.workbench.util.SeverityIcon
import org.ossreviewtoolkit.workbench.util.WebLink

@Composable
fun Dependencies(viewModel: DependenciesViewModel) {
    val state by viewModel.state.collectAsState()

    when {
        !state.initialized -> {
            LaunchedEffect(Unit) {
                state.initialize()
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(25.dp, alignment = Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text("Processing...")
            }
        }

        state.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ErrorCard(state.error.orEmpty())
            }
        }

        else -> {
            Column {
                TitleRow(
                    search = state.search,
                    searchCurrentHit = state.searchCurrentHit,
                    searchTotalHits = state.searchHits.size,
                    onSearchChange = { state.updateSearch(it) },
                    onSelectNextSearchHit = { state.selectNextSearchHit() },
                    onSelectPreviousSearchHit = { state.selectPreviousSearchHit() }
                )

                Row {
                    Card(modifier = Modifier.weight(1f).padding(15.dp), elevation = 8.dp) {
                        Row(
                            modifier = Modifier.padding(top = 15.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                DependencyTree(state)
                            }
                        }
                    }

                    state.selectedItem.let { item ->
                        AnimatedVisibility(visible = item != null) {
                            Surface(
                                modifier = Modifier.width(500.dp).fillMaxHeight(),
                                color = MaterialTheme.colors.background,
                                elevation = 8.dp
                            ) {
                                if (item != null) {
                                    Column {
                                        when (item) {
                                            is DependencyTreeProject -> ProjectDetails(item)
                                            is DependencyTreeScope -> ScopeDetails(item)
                                            is DependencyTreePackage -> PackageDetails(item)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TitleRow(
    search: String,
    searchCurrentHit: Int,
    searchTotalHits: Int,
    onSearchChange: (String) -> Unit,
    onSelectNextSearchHit: () -> Unit,
    onSelectPreviousSearchHit: () -> Unit
) {
    TopAppBar(
        modifier = Modifier.zIndex(1f),
        backgroundColor = MaterialTheme.colors.primary,
        title = {},
        actions = {
            Search(
                search = search,
                searchCurrentHit = searchCurrentHit,
                searchTotalHits = searchTotalHits,
                onSearchChange = onSearchChange,
                onSelectNextSearchHit = onSelectNextSearchHit,
                onSelectPreviousSearchHit = onSelectPreviousSearchHit
            )
        }
    )
}

@Composable
private fun Search(
    search: String,
    searchCurrentHit: Int,
    searchTotalHits: Int,
    onSearchChange: (String) -> Unit,
    onSelectNextSearchHit: () -> Unit,
    onSelectPreviousSearchHit: () -> Unit
) {
    fun handleSearchKeyEvent(event: KeyEvent) =
        when (event.type) {
            KeyEventType.KeyDown -> {
                when (event.key) {
                    Key.Enter -> {
                        onSelectNextSearchHit()
                        true
                    }

                    else -> false
                }
            }

            KeyEventType.KeyUp -> {
                when (event.key) {
                    Key.DirectionDown -> {
                        onSelectNextSearchHit()
                        true
                    }

                    Key.DirectionUp -> {
                        onSelectPreviousSearchHit()
                        true
                    }

                    else -> false
                }
            }

            else -> false
        }

    Box(modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp)) {
        FilterTextField(
            filterText = search,
            label = "Search",
            icon = MaterialIcon.SEARCH,
            modifier = Modifier.onKeyEvent(::handleSearchKeyEvent),
            onSearchChange
        )
    }

    Row(
        modifier = Modifier.width(100.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (search.isNotBlank()) {
            Text("${searchCurrentHit + 1} / $searchTotalHits", modifier = Modifier.padding(start = 10.dp))

            Icon(
                painterResource(MaterialIcon.EXPAND_LESS.resource),
                contentDescription = "previous",
                modifier = Modifier.clickable { onSelectPreviousSearchHit() }
            )

            Icon(
                painterResource(MaterialIcon.EXPAND_MORE.resource),
                contentDescription = "next",
                modifier = Modifier.clickable { onSelectNextSearchHit() }
            )
        }
    }
}

@Composable
@Preview
private fun TitleRowPreview() {
    Preview {
        TitleRow("some-package", 2, 10, onSearchChange = {}, onSelectNextSearchHit = {}, onSelectPreviousSearchHit = {})
    }
}

@Composable
fun DependencyTree(
    state: DependenciesState,
    indentationPerLevel: Int = 10
) {
    // TODO: Use different icons for collapsing.
    // TODO: Add icons for item type.
    // TODO: Use different color for items with issues.
    Box(modifier = Modifier.fillMaxSize()) {
        val listState = rememberLazyListState()

        if (state.isItemAutoSelected) {
            val selectedItemIndex = state.filteredDependencyTreeItems.indexOf(state.selectedItem)
            if (selectedItemIndex >= 0) {
                LaunchedEffect(selectedItemIndex) {
                    listState.animateScrollToItem(selectedItemIndex)
                }
            }
        }

        LazyColumn(Modifier.fillMaxSize().padding(top = 5.dp, start = 15.dp, end = 15.dp), listState) {
            items(state.filteredDependencyTreeItems.size, key = { it }) { index ->
                val item = state.filteredDependencyTreeItems[index]

                Row(
                    modifier = Modifier.padding(start = (item.level * indentationPerLevel).dp)
                        .padding(top = if (item.level == 0) 10.dp else 2.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    val resource = when {
                        !item.hasChildren -> MaterialIcon.BOOKMARK_BORDER.resource
                        item.expanded -> MaterialIcon.REMOVE.resource
                        else -> MaterialIcon.ADD.resource
                    }

                    val icon = painterResource(resource)

                    Icon(
                        painter = icon,
                        contentDescription = "expand",
                        modifier = Modifier.size(12.dp).clickable { state.toggleExpanded(item) }
                    )

                    Text(
                        item.name,
                        modifier = Modifier.clickable { state.selectItem(item, isAutoSelected = false) },
                        fontWeight = if (item.index == state.selectedItem?.index) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState = listState)
        )
    }
}

@Composable
fun ProjectDetails(item: DependencyTreeProject) {
    val scrollState = rememberScrollState()
    val pkg = remember { item.project.toPackage() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(15.dp)) {
            Text(text = "${item.project.id.name} ${item.project.id.version}", style = MaterialTheme.typography.h4)

            Column(modifier = Modifier.verticalScroll(scrollState).padding(top = 15.dp)) {
                CaptionedText("DEFINITION FILE", item.project.definitionFilePath)

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                IdentifierSection(pkg)

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                CopyrightSection(pkg, item.resolvedLicense)

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                LicenseSection(pkg, item.resolvedLicense)

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                ProjectProvenanceSection(item.project)

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                IssuesSection(item.issues)

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                // TODO: Add vulnerability section.

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val homepageUrl = pkg.homepageUrl
                    if (homepageUrl.isNotBlank()) {
                        WebLink("Homepage", homepageUrl)
                    }
                }
            }
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState = scrollState)
        )
    }
}

@Composable
fun ScopeDetails(item: DependencyTreeScope) {
    val dependencyCount = rememberSaveable("${item.project.id.toCoordinates()}:${item.scope.name}") {
        item.scope.collectDependencies().size
    }

    Column(modifier = Modifier.padding(15.dp)) {
        Text(
            text = "${item.project.id.name} ${item.project.id.version} - ${item.scope.name} scope",
            style = MaterialTheme.typography.h4
        )

        Text("This scope contains $dependencyCount dependencies.", modifier = Modifier.padding(top = 15.dp))
    }
}

@Composable
fun PackageDetails(item: DependencyTreePackage) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(15.dp)) {
            Text(text = "${item.id.name} ${item.id.version}", style = MaterialTheme.typography.h4)

            Column(modifier = Modifier.verticalScroll(scrollState).padding(top = 15.dp)) {
                if (item.pkg == null) {
                    Text("No package information found.")

                    Divider(modifier = Modifier.padding(vertical = 10.dp))
                } else {
                    IdentifierSection(item.pkg.metadata)

                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                    CopyrightSection(item.pkg.metadata, item.resolvedLicense)

                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                    LicenseSection(item.pkg.metadata, item.resolvedLicense)

                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                    DescriptionSection(item.pkg.metadata.description)

                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                    PackageProvenanceSection(item.pkg.metadata)

                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                    CurationSection(item.pkg.curations)

                    Divider(modifier = Modifier.padding(vertical = 10.dp))
                }

                IssuesSection(item.issues)

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                // TODO: Add vulnerability section.

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val homepageUrl = item.pkg?.metadata?.homepageUrl.orEmpty()
                    if (homepageUrl.isNotBlank()) {
                        WebLink("Homepage", homepageUrl)
                    }
                }
            }
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState = scrollState)
        )
    }
}

@Composable
@Preview
private fun PackageDetailsPreview() {
    val pkg = CuratedPackage(
        metadata = Package(
            id = Identifier("Maven:com.example:package:1.0.0"),
            authors = sortedSetOf("Jane Doe", "John Doe"),
            declaredLicenses = sortedSetOf("Apache-2.0", "LicenseRef-esoteric-license"),
            declaredLicensesProcessed = ProcessedDeclaredLicense(
                SpdxExpression.Companion.parse("Apache-2.0 AND LicenseRef-esoteric-license")
            ),
            concludedLicense = SpdxLicense.APACHE_2_0.toExpression(),
            description = "This is the example description of the example package.",
            homepageUrl = "https://example.com",
            binaryArtifact = RemoteArtifact.EMPTY.copy(url = "https://example.com/package-1.0.0.jar"),
            sourceArtifact = RemoteArtifact.EMPTY.copy(url = "https://example.com/package-1.0.0-sources.jar"),
            vcs = VcsInfo(VcsType.GIT, "https://example.com/package.git", "master", "path"),
            isMetadataOnly = true,
            isModified = true
        )
    )

    val resolvedLicense = ResolvedLicenseInfo(
        id = pkg.metadata.id,
        licenseInfo = LicenseInfo(
            id = pkg.metadata.id,
            declaredLicenseInfo = DeclaredLicenseInfo(
                pkg.metadata.authors,
                pkg.metadata.declaredLicenses,
                pkg.metadata.declaredLicensesProcessed,
                emptyList()
            ),
            detectedLicenseInfo = DetectedLicenseInfo(emptyList()),
            concludedLicenseInfo = ConcludedLicenseInfo(null, emptyList())
        ),
        licenses = emptyList(),
        copyrightGarbage = emptyMap(),
        unmatchedCopyrights = emptyMap()
    )

    val issue = OrtIssue(
        source = pkg.metadata.id.type,
        message = "Some long message. ".repeat(20)
    )

    Preview {
        Text("test")
        PackageDetails(
            DependencyTreePackage(
                index = 0,
                level = 0,
                hasChildren = true,
                id = pkg.metadata.id,
                pkg = pkg,
                linkage = PackageLinkage.STATIC,
                issues = listOf(issue),
                resolvedLicense = resolvedLicense
            )
        )
    }
}

@Composable
fun IdentifierSection(pkg: Package) {
    Expandable(header = {
        CaptionedText("IDENTIFIER", pkg.id.toCoordinates())
    }) {
        Column(
            modifier = Modifier.padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CaptionedText("PURL", pkg.purl)
            CaptionedText("CPE", pkg.cpe ?: "-")
            CaptionedColumn("PROPERTIES") {
                Text("isMetadataOnly = ${pkg.isMetadataOnly}", style = MaterialTheme.typography.overline)
                Text("isModified = ${pkg.isModified}", style = MaterialTheme.typography.overline)
            }
        }
    }
}

@Composable
fun CopyrightSection(pkg: Package, resolvedLicense: ResolvedLicenseInfo?) {
    Expandable(header = {
        CaptionedText("AUTHORS", pkg.authors.joinToString())
    }) {
        val copyrights = remember(pkg.id) { resolvedLicense?.getCopyrights().orEmpty().joinToString(separator = "\n") }
        CaptionedText("COPYRIGHTS", copyrights, modifier = Modifier.padding(top = 10.dp))
    }
}

@Composable
fun LicenseSection(pkg: Package, resolvedLicense: ResolvedLicenseInfo?) {
    Expandable(header = {
        val effectiveLicense = remember(pkg.id) {
            resolvedLicense?.effectiveLicense(LicenseView.CONCLUDED_OR_DECLARED_AND_DETECTED)
        }

        CaptionedText("EFFECTIVE LICENSE", effectiveLicense?.toString() ?: "-")
    }) {
        val detectedLicense = remember(pkg.id) { resolvedLicense?.effectiveLicense(LicenseView.ONLY_DETECTED) }

        Column(
            modifier = Modifier.padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CaptionedText("DECLARED LICENSE", pkg.declaredLicensesProcessed.spdxExpression?.toString() ?: "-")
            CaptionedText("DETECTED LICENSE", detectedLicense?.toString() ?: "-")
            CaptionedText("CONCLUDED LICENSE", pkg.concludedLicense?.toString() ?: "-")
            // TODO: Add declared license mapping and unmapped licenses.
        }
    }
}

@Composable
fun DescriptionSection(description: String, lineLengthCollapsed: Int = 50) {
    val start = description.take(lineLengthCollapsed)
        .let { if (it.length == lineLengthCollapsed) it.substringBeforeLast(" ") else it }
    val end = if (start.length < description.length) description.substring(start.length).trimStart() else ""

    Expandable(header = { expanded ->
        CaptionedText("DESCRIPTION", start + if (!expanded && end.isNotEmpty()) "..." else "")
    }) {
        if (end.isNotEmpty()) Text(end)
    }
}

@Composable
fun PackageProvenanceSection(pkg: Package) {
    Expandable(header = { expanded ->
        CaptionedColumn("BINARY ARTIFACT") {
            if (pkg.binaryArtifact != RemoteArtifact.EMPTY) {
                Text(pkg.binaryArtifact.url)
                if (expanded && pkg.binaryArtifact.hash != Hash.NONE) {
                    Text(
                        text = "${pkg.binaryArtifact.hash.algorithm}: ${pkg.binaryArtifact.hash.value}",
                        style = MaterialTheme.typography.overline,
                        modifier = Modifier.padding(start = 5.dp)
                    )
                }
            } else {
                Text("-")
            }
        }
    }) {
        Column(
            modifier = Modifier.padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CaptionedColumn("SOURCE ARTIFACT") {
                if (pkg.sourceArtifact != RemoteArtifact.EMPTY) {
                    Text(pkg.sourceArtifact.url)
                    if (pkg.sourceArtifact.hash != Hash.NONE) {
                        Text(
                            text = "${pkg.sourceArtifact.hash.algorithm}: ${pkg.sourceArtifact.hash.value}",
                            style = MaterialTheme.typography.overline,
                            modifier = Modifier.padding(start = 5.dp)
                        )
                    }
                } else {
                    Text("-")
                }
            }

            RepositoryColumn(pkg.vcsProcessed, expanded = true)

            // TODO: Show scanned provenances.
        }
    }
}

@Composable
fun ProjectProvenanceSection(project: Project) {
    Expandable(header = { expanded ->
        RepositoryColumn(project.vcs, expanded)
    }) {}
}

@Composable
fun RepositoryColumn(vcs: VcsInfo, expanded: Boolean) {
    CaptionedColumn("REPOSITORY") {
        if (vcs != VcsInfo.EMPTY) {
            Text(vcs.url)
            if (expanded) {
                Text(
                    text = "Type: ${vcs.type}",
                    style = MaterialTheme.typography.overline,
                    modifier = Modifier.padding(start = 5.dp)
                )
                if (vcs.path.isNotBlank()) {
                    Text(
                        text = "Path: ${vcs.path}",
                        style = MaterialTheme.typography.overline,
                        modifier = Modifier.padding(start = 5.dp)
                    )
                }
                if (vcs.revision.isNotBlank()) {
                    Text(
                        text = "Revision: ${vcs.revision}",
                        style = MaterialTheme.typography.overline,
                        modifier = Modifier.padding(start = 5.dp)
                    )
                }
            }
        } else {
            Text("-")
        }
    }
}

@Composable
fun CurationSection(curations: List<PackageCurationResult>) {
    Expandable(header = {
        CaptionedText(
            caption = "CURATIONS",
            text = "${if (curations.isEmpty()) "No" else curations.size} curation(s) were applied to this package."
        )
    }) {
        Column(
            modifier = Modifier.padding(top = 5.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            curations.forEach { curation ->
                Divider(thickness = 0.5.dp)

                ProvideTextStyle(MaterialTheme.typography.overline) {
                    Text("Comment: ${curation.curation.comment ?: "-"}")

                    curation.curation.purl?.let { purl ->
                        Text("PURL: ${curation.base.purl} -> $purl")
                    }

                    curation.curation.cpe?.let { cpe ->
                        Text("CPE: ${curation.base.cpe} -> $cpe")
                    }

                    curation.curation.authors?.let { authors ->
                        Text("Authors: ${curation.base.authors} -> $authors")
                    }

                    curation.curation.concludedLicense?.let { concludedLicense ->
                        Text("Concluded license: ${curation.base.concludedLicense} -> $concludedLicense")
                    }

                    curation.curation.description?.let { description ->
                        Text("Description: ${curation.base.description} -> $description")
                    }

                    curation.curation.homepageUrl?.let { homepageUrl ->
                        Text("Homepage: ${curation.base.homepageUrl} -> $homepageUrl")
                    }

                    curation.curation.binaryArtifact?.let { binaryArtifact ->
                        Text("Binary artifact: ${curation.base.binaryArtifact} -> $binaryArtifact")
                    }

                    curation.curation.vcs?.let { vcs ->
                        Text("VCS: ${curation.base.vcs} -> $vcs")
                    }

                    curation.curation.isMetadataOnly?.let { isMetadataOnly ->
                        Text("Is metadata only: ${curation.base.isMetadataOnly} -> $isMetadataOnly")
                    }

                    curation.curation.isModified?.let { isModified ->
                        Text("Is modified: ${curation.base.isModified} -> $isModified")
                    }

                    if (curation.curation.declaredLicenseMapping.isNotEmpty()) {
                        Text("Declared license mapping: ${curation.curation.declaredLicenseMapping}")
                    }
                }
            }
        }
    }
}

@Composable
fun IssuesSection(issues: List<OrtIssue>) {
    // TODO: Support resolutions.
    Expandable(
        header = {
            val text = if (issues.size == 1) {
                "There was 1 issue during dependency resolution."
            } else {
                "There were ${issues.size} issues during dependency resolution."
            }
            CaptionedText("ISSUES", text)
        }
    ) {
        Column(
            modifier = Modifier.padding(top = 5.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            issues.forEach { issue ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    SeverityIcon(issue.severity)
                    Box(modifier = Modifier.weight(1f))
                    Text(issue.source)
                }

                ExpandableText(issue.message)
            }
        }
    }
}
