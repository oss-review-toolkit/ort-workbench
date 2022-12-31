package org.ossreviewtoolkit.workbench.ui.summary

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import org.ossreviewtoolkit.workbench.composables.Link
import org.ossreviewtoolkit.workbench.composables.Preview
import org.ossreviewtoolkit.workbench.composables.StyledCard
import org.ossreviewtoolkit.workbench.ui.MainScreen
import org.ossreviewtoolkit.workbench.utils.MaterialIcon

private const val KIBI = 1024

@Composable
fun Summary(viewModel: SummaryViewModel, onSwitchScreen: (MainScreen) -> Unit, onOpenResult: () -> Unit) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.padding(15.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(25.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                ResultFileInfoCard(state.resultFileInfo, onOpenResult)

                DependencyStatsCard(state.dependencyStats) { onSwitchScreen(MainScreen.Dependencies) }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                IssueStatsCard(state.issueStats) { onSwitchScreen(MainScreen.Issues) }
            }
        }
    }
}

@Composable
fun ResultFileInfoCard(info: ResultFileInfo, onOpenResult: () -> Unit) {
    StyledCard(
        titleIcon = painterResource(MaterialIcon.FILE_PRESENT.resource),
        title = "ORT Result File"
    ) {
        Row(modifier = Modifier.padding(vertical = 15.dp)) {
            Column {
                Text("Path:")
                Text("Size:")
            }

            Column {
                Text(info.absolutePath)
                Text("${info.size / KIBI} kb")
            }
        }

        Link("Load new file", onClick = onOpenResult)
    }
}

@Composable
@Preview
private fun ResultFileInfoCardPreview() {
    Preview {
        ResultFileInfoCard(ResultFileInfo("/some/path", 1024 * 1024)) {}
    }
}

@Composable
fun IssueStatsCard(
    stats: IssueStats,
    onClickDetails: () -> Unit
) {
    StyledCard(
        titleIcon = painterResource(MainScreen.Issues.icon.resource),
        title = "Issue Stats"
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(count = 5),
            modifier = Modifier.padding(vertical = 15.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            item { Text("") }
            item { Text("Total", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold) }
            item { Text("Analyzer", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold) }
            item { Text("Advisor", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold) }
            item { Text("Scanner", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold) }

            item { Text("Errors", fontWeight = FontWeight.Bold) }
            item { Text(stats.totalIssues.errors.toString(), textAlign = TextAlign.Center) }
            item { Text(stats.analyzerIssues.errors.toString(), textAlign = TextAlign.Center) }
            item { Text(stats.advisorIssues.errors.toString(), textAlign = TextAlign.Center) }
            item { Text(stats.scannerIssues.errors.toString(), textAlign = TextAlign.Center) }

            item { Text("Warnings", fontWeight = FontWeight.Bold) }
            item { Text(stats.totalIssues.warnings.toString(), textAlign = TextAlign.Center) }
            item { Text(stats.analyzerIssues.warnings.toString(), textAlign = TextAlign.Center) }
            item { Text(stats.advisorIssues.warnings.toString(), textAlign = TextAlign.Center) }
            item { Text(stats.scannerIssues.warnings.toString(), textAlign = TextAlign.Center) }

            item { Text("Hints", fontWeight = FontWeight.Bold) }
            item { Text(stats.totalIssues.hints.toString(), textAlign = TextAlign.Center) }
            item { Text(stats.analyzerIssues.hints.toString(), textAlign = TextAlign.Center) }
            item { Text(stats.advisorIssues.hints.toString(), textAlign = TextAlign.Center) }
            item { Text(stats.scannerIssues.hints.toString(), textAlign = TextAlign.Center) }
        }

        Link("Details") { onClickDetails() }
    }
}

@Composable
@Preview
private fun IssueStatsCardPreview() {
    Preview {
        IssueStatsCard(stats = IssueStats.EMPTY) { }
    }
}

@Composable
fun DependencyStatsCard(stats: DependencyStats, onClickDetails: () -> Unit) {
    StyledCard(
        titleIcon = painterResource(MainScreen.Dependencies.icon.resource),
        title = "Dependency Stats"
    ) {
        Text(
            "${stats.projectsTotal} Projects",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Text(
            stats.projectsByPackageManager.entries.joinToString { "${it.value} ${it.key}" },
            fontStyle = FontStyle.Italic
        )

        Divider(modifier = Modifier.padding(vertical = 10.dp))

        Text(
            "${stats.dependenciesTotal} Dependencies",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Text(
            stats.dependenciesByPackageManager.entries.joinToString { "${it.value} ${it.key}" },
            fontStyle = FontStyle.Italic
        )

        Divider(modifier = Modifier.padding(vertical = 10.dp))

        Link("Details") { onClickDetails() }
    }
}

@Composable
@Preview
private fun DependencyStatsCardPreview() {
    Preview {
        val stats = DependencyStats(
            projectsTotal = 5,
            projectsByPackageManager = mapOf("Gradle" to 2, "NPM" to 3),
            dependenciesTotal = 10,
            dependenciesByPackageManager = mapOf("Gradle" to 4, "NPM" to 6)
        )
        DependencyStatsCard(stats) {}
    }
}
