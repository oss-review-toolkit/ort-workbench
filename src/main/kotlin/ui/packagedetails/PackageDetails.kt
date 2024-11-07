package org.ossreviewtoolkit.workbench.ui.packagedetails

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import org.ossreviewtoolkit.model.Package
import org.ossreviewtoolkit.model.PackageCurationResult
import org.ossreviewtoolkit.model.Project
import org.ossreviewtoolkit.model.RemoteArtifact
import org.ossreviewtoolkit.model.licenses.LicenseView
import org.ossreviewtoolkit.model.licenses.ResolvedLicenseInfo
import org.ossreviewtoolkit.workbench.composables.ScreenAppBar
import org.ossreviewtoolkit.workbench.composables.WebLink
import org.ossreviewtoolkit.workbench.composables.toStringOrDash

private const val TAB_METADATA = 0
private const val TAB_SCAN_RESULTS = 1

@Composable
fun PackageDetails(state: PackageDetailsState, onBack: () -> Unit) {
    if (state.packageInfo == null) {
        // TODO: Show package not found message.
        return
    }

    Column {
        ScreenAppBar(
            title = { Text(state.packageInfo.id.toCoordinates(), style = MaterialTheme.typography.h3) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        )

        var tabIndex by remember { mutableStateOf(TAB_METADATA) }

        TabRow(selectedTabIndex = tabIndex) {
            Tab(
                text = { Text("Metadata") },
                selected = tabIndex == TAB_METADATA,
                onClick = { tabIndex = TAB_METADATA }
            )

            Tab(
                text = { Text("Scan Results") },
                selected = tabIndex == TAB_SCAN_RESULTS,
                onClick = { tabIndex = TAB_SCAN_RESULTS }
            )
        }

        when (tabIndex) {
            TAB_METADATA -> PackageMetadata(state.packageInfo)
            TAB_SCAN_RESULTS -> PackageScanResults()
        }
    }
}

@Composable
private fun PackageMetadata(packageInfo: PackageInfo) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                if (packageInfo.pkg == null) {
                    Text("Could not find package '${packageInfo.id.toCoordinates()}'.")
                }

                if (packageInfo.pkg != null) {
                    PackageIdentity(packageInfo.pkg.metadata)
                }

                if (packageInfo.project != null) {
                    ProjectInfo(packageInfo.project)
                }

                if (packageInfo.pkg != null) {
                    PackageLicense(packageInfo.license)
                    PackageDescription(packageInfo.pkg.metadata)
                    PackageArtifacts(packageInfo.pkg.metadata)
                    PackageRepository(packageInfo.pkg.metadata)
                    PackageCurations(packageInfo.pkg.curations)
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
private fun PackageDetailsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.width(800.dp), elevation = 8.dp) {
        Column(modifier = Modifier.padding(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.padding(horizontal = 10.dp)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.h5)
            }

            Divider()

            Column(modifier = Modifier.padding(horizontal = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun PackageDetailsRow(key: String, content: @Composable RowScope.() -> Unit) {
    Row {
        Text(key, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        content()
    }
}

@Composable
private fun PackageDetailsRow(key: String, value: String, url: String? = null) {
    PackageDetailsRow(key) {
        if (url.isNullOrEmpty()) {
            Text(value)
        } else {
            WebLink(value, url)
        }
    }
}

@Composable
private fun PackageIdentity(pkg: Package) {
    PackageDetailsCard("Identity") {
        PackageDetailsRow("Identifier", pkg.id.toCoordinates())
        PackageDetailsRow("purl", pkg.purl)
        PackageDetailsRow("CPE", pkg.cpe.toStringOrDash())
    }
}

@Composable
private fun ProjectInfo(project: Project) {
    PackageDetailsCard("Project") {
        PackageDetailsRow("Definition file", project.definitionFilePath)
        PackageDetailsRow("Scopes", project.scopes.joinToString { it.name })
    }
}

@Composable
private fun PackageLicense(license: ResolvedLicenseInfo) {
    PackageDetailsCard("License") {
        PackageDetailsRow(
            "Effective",
            license.effectiveLicense(LicenseView.CONCLUDED_OR_DECLARED_AND_DETECTED).toStringOrDash()
        )
        PackageDetailsRow(
            "Concluded",
            license.effectiveLicense(LicenseView.ONLY_CONCLUDED).toStringOrDash()
        )
        PackageDetailsRow(
            "Declared",
            license.effectiveLicense(LicenseView.ONLY_DECLARED).toStringOrDash()
        )
        PackageDetailsRow(
            "Detected",
            license.effectiveLicense(LicenseView.ONLY_DETECTED).toStringOrDash()
        )
    }
}

@Composable
private fun PackageDescription(pkg: Package) {
    PackageDetailsCard("Description") {
        PackageDetailsRow("Homepage", pkg.homepageUrl.toStringOrDash(), url = pkg.homepageUrl)
        PackageDetailsRow("Authors", pkg.authors.joinToString().toStringOrDash())
        PackageDetailsRow("Description", pkg.description.toStringOrDash())
    }
}

@Composable
private fun ArtifactRow(key: String, artifact: RemoteArtifact) {
    PackageDetailsRow(key) {
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(5.dp)) {
            if (artifact.url.isEmpty()) {
                Text(artifact.url.toStringOrDash())
            } else {
                WebLink(artifact.url, artifact.url)
            }

            if (artifact.hash.value.isNotEmpty()) {
                Text("${artifact.hash.algorithm}: ${artifact.hash.value}")
            }
        }
    }
}

@Composable
private fun PackageArtifacts(pkg: Package) {
    PackageDetailsCard("Artifacts") {
        ArtifactRow("Binary artifact", pkg.binaryArtifact)
        ArtifactRow("Source artifact", pkg.sourceArtifact)
    }
}

@Composable
private fun PackageRepository(pkg: Package) {
    PackageDetailsCard("Repository") {
        PackageDetailsRow("Type", pkg.vcsProcessed.type.toStringOrDash())
        PackageDetailsRow("URL", pkg.vcsProcessed.url.toStringOrDash(), pkg.vcsProcessed.url)
        PackageDetailsRow("Revision", pkg.vcsProcessed.revision.toStringOrDash())
        PackageDetailsRow("Path", pkg.vcsProcessed.path.toStringOrDash())
    }
}

@Composable
private fun PackageCurations(curations: List<PackageCurationResult>) {
    PackageDetailsCard("Package Curations") {
        if (curations.isEmpty()) {
            Text("No package curations were applied.")
        } else {
            // TODO: Show package curations.
            Text("Coming soon...")
        }
    }
}

@Composable
private fun PackageScanResults() {
    Column(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Coming soon...")
    }
}
