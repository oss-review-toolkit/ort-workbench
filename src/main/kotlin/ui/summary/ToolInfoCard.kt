package org.ossreviewtoolkit.workbench.ui.summary

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp

import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.CodeBlockStyle
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.material.RichText

import org.jetbrains.compose.resources.painterResource

import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.reporter.IssueStatistics
import org.ossreviewtoolkit.workbench.composables.Datetime
import org.ossreviewtoolkit.workbench.composables.Expandable
import org.ossreviewtoolkit.workbench.composables.SeverityIcon
import org.ossreviewtoolkit.workbench.composables.StyledCard
import org.ossreviewtoolkit.workbench.composables.TwoColumnTable
import org.ossreviewtoolkit.workbench.utils.OrtIcon

private val whiteCodeBlockRichTextStyle = RichTextStyle(codeBlockStyle = CodeBlockStyle(modifier = Modifier))

@Suppress("LongMethod")
@Composable
fun ToolInfoCard(
    icon: OrtIcon,
    toolName: String,
    info: ToolInfo,
    startExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    StyledCard(
        titleIcon = painterResource(icon.resource),
        title = toolName
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Default.CalendarMonth),
                        contentDescription = "Start time"
                    )

                    Datetime(info.startTime)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Default.Timelapse),
                        contentDescription = "Duration"
                    )

                    val formattedDuration = remember(info.duration) { info.duration.toString() }
                    Text(formattedDuration)
                }
            }

            Column(modifier = Modifier.weight(1f).padding(top = 4.dp), content = content)

            if (info.serializedConfig != null) {
                Column(modifier = Modifier.weight(0.75f)) {
                    Text("Config", style = MaterialTheme.typography.h6)

                    val configMarkdown = remember(info.serializedConfig) {
                        """
                        |``` yaml
                        |${info.serializedConfig}
                        |```
                    """.trimMargin()
                    }

                    Box(
                        modifier = Modifier.sizeIn(
                            minHeight = 20.dp,
                            maxHeight = 150.dp
                        )
                    ) {
                        val horizontalScrollState = rememberScrollState()
                        val verticalScrollState = rememberScrollState()

                        Column(
                            modifier = Modifier
                                .horizontalScroll(horizontalScrollState)
                                .verticalScroll(verticalScrollState)
                        ) {
                            RichText(style = whiteCodeBlockRichTextStyle) {
                                Markdown(configMarkdown)
                            }
                        }

                        Box(
                            modifier = Modifier.matchParentSize()
                        ) {
                            HorizontalScrollbar(
                                modifier = Modifier.align(Alignment.BottomEnd),
                                adapter = rememberScrollbarAdapter(scrollState = horizontalScrollState)
                            )

                            VerticalScrollbar(
                                modifier = Modifier.align(Alignment.CenterEnd),
                                adapter = rememberScrollbarAdapter(scrollState = verticalScrollState)
                            )
                        }
                    }
                }
            }
        }

        if (info.environment == null && info.environmentVariables == null) {
            IssueRow(info.issueStats)
        } else {
            Expandable(
                header = { IssueRow(info.issueStats) },
                startExpanded = startExpanded
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 16.dp)) {
                    info.environment?.let { environment ->
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Environment", style = MaterialTheme.typography.h6)

                            TwoColumnTable(headers = "Property" to "Value", data = environment)
                        }
                    }

                    info.environmentVariables?.let { environmentVariables ->
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Environment Variables", style = MaterialTheme.typography.h6)

                            TwoColumnTable(headers = "Variable" to "Value", data = environmentVariables)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IssueRow(issueStats: IssueStatistics) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    ) {
        IssueCounter(Severity.ERROR, issueStats.errors)
        IssueCounter(Severity.WARNING, issueStats.warnings)
        IssueCounter(Severity.HINT, issueStats.hints)
    }
}

@Composable
private fun IssueCounter(severity: Severity, issueCount: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        SeverityIcon(severity, resolved = issueCount == 0, size = 20.dp)
        Text("$issueCount")
    }
}
