package org.ossreviewtoolkit.workbench.ui.packagedetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.workbench.composables.ScreenAppBar
import org.ossreviewtoolkit.workbench.utils.MaterialIcon

private const val TAB_METADATA = 0
private const val TAB_SCAN_RESULTS = 1

@Composable
fun PackageDetails(id: Identifier, onBack: () -> Unit) {
    Column {
        ScreenAppBar(
            title = { Text(id.toCoordinates(), style = MaterialTheme.typography.h3) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        painterResource(MaterialIcon.ARROW_BACK.resource),
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
            TAB_METADATA -> PackageMetadata()
            TAB_SCAN_RESULTS -> PackageScanResults()
        }
    }
}

@Composable
fun PackageMetadata() {
    Text("Metadata")
}

@Composable
fun PackageScanResults() {
    Text("Scan Results")
}
