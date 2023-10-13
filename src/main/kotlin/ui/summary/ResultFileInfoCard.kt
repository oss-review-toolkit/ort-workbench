package org.ossreviewtoolkit.workbench.ui.summary

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.RichText

import com.seanproctor.datatable.DataColumn
import com.seanproctor.datatable.material.DataTable

import java.time.Instant

import org.ossreviewtoolkit.model.VcsInfo
import org.ossreviewtoolkit.model.VcsType
import org.ossreviewtoolkit.workbench.composables.Preview
import org.ossreviewtoolkit.workbench.composables.SingleLineText
import org.ossreviewtoolkit.workbench.composables.StyledCard
import org.ossreviewtoolkit.workbench.composables.TwoColumnTable
import org.ossreviewtoolkit.workbench.composables.rememberFormattedDatetime
import org.ossreviewtoolkit.workbench.utils.MaterialIcon

private const val KIBI = 1024
private const val MEBI = KIBI * KIBI

@Composable
fun ResultFileInfoCard(info: ResultFileInfo) {
    StyledCard(
        titleIcon = painterResource(MaterialIcon.FILE_PRESENT.resource),
        title = "ORT Result File"
    ) {
        Row(modifier = Modifier.padding(vertical = 15.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(0.75f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("File Info", style = MaterialTheme.typography.h6)

                TwoColumnTable(
                    headers = "Property" to "Value",
                    data = mapOf(
                        "Path" to info.absolutePath,
                        "Size" to "%.2f MiB".format(info.size.toFloat() / MEBI),
                        "Modified" to rememberFormattedDatetime(info.timestamp)
                    )
                )
            }

            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Repository Configuration", style = MaterialTheme.typography.h6)

                val stats = info.repositoryConfigurationStats

                val markdown = """
                    * Defines ${stats.pathExcludeCount} path exclude(s) and ${stats.scopeExcludeCount} scope exclude(s).
                    * Contains resolutions for ${stats.issueResolutionCount} issues,
                      ${stats.vulnerabilityResolutionCount} vulnerabilities, and ${stats.ruleViolationResolutionCount}
                      rule violations.
                    * Contains ${stats.packageCurationCount} package curation(s) and ${stats.packageConfigurationCount}
                      package configuration(s).
                    * Contains ${stats.licenseFindingCurationCount} license finding curation(s) for the project's own
                      source code.
                    * Contains ${stats.licenseChoiceCount} license choice(s).
                """.trimIndent()

                RichText(modifier = Modifier.padding(top = 8.dp)) { Markdown(markdown) }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("VCS Info", style = MaterialTheme.typography.h6)

            DataTable(
                modifier = Modifier.fillMaxWidth(),
                headerHeight = 36.dp,
                rowHeight = 24.dp,
                columns = listOf(
                    DataColumn { SingleLineText("Path") },
                    DataColumn { SingleLineText("URL") },
                    DataColumn { SingleLineText("Revision") },
                    DataColumn { SingleLineText("Sparse Checkout Path") }
                )
            ) {
                row {
                    cell { SingleLineText("/") }
                    cell { SingleLineText(info.vcs.url) }
                    cell { SingleLineText(info.vcs.revision) }
                    cell { SingleLineText(info.vcs.path) }
                }

                info.nestedRepositories.forEach { (path, vcs) ->
                    row {
                        cell { SingleLineText("/$path") }
                        cell { SingleLineText(vcs.url) }
                        cell { SingleLineText(vcs.revision) }
                        cell { SingleLineText(vcs.path) }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun ResultFileInfoCardPreview() {
    Preview {
        ResultFileInfoCard(
            ResultFileInfo(
                absolutePath = "/some/path",
                size = 1024 * 1024,
                timestamp = Instant.now(),
                vcs = VcsInfo(
                    type = VcsType.GIT,
                    url = "https://example.org/repo.git",
                    revision = "main",
                    path = "path"
                ),
                nestedRepositories = mapOf(
                    "submodule1" to VcsInfo(
                        type = VcsType.GIT,
                        url = "https://example.org/submodule1.git",
                        revision = "main"
                    ),
                    "submodule2" to VcsInfo(
                        type = VcsType.GIT,
                        url = "https://example.org/submodule2.git",
                        revision = "main"
                    )
                ),
                repositoryConfigurationStats = RepositoryConfigurationStats(1, 2, 3, 4, 5, 6, 7, 8, 9)
            )
        )
    }
}
