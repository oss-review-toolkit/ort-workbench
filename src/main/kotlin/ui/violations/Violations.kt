package org.ossreviewtoolkit.workbench.ui.violations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.LicenseSource
import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.model.TextLocation
import org.ossreviewtoolkit.model.config.LicenseFindingCurationReason
import org.ossreviewtoolkit.model.config.RuleViolationResolution
import org.ossreviewtoolkit.model.config.RuleViolationResolutionReason
import org.ossreviewtoolkit.utils.common.titlecase
import org.ossreviewtoolkit.utils.spdx.SpdxLicenseIdExpression
import org.ossreviewtoolkit.utils.spdx.SpdxSingleLicenseExpression
import org.ossreviewtoolkit.workbench.composables.CircularProgressBox
import org.ossreviewtoolkit.workbench.composables.ExpandableMarkdown
import org.ossreviewtoolkit.workbench.composables.ExpandableText
import org.ossreviewtoolkit.workbench.composables.FilterButton
import org.ossreviewtoolkit.workbench.composables.FilterPanel
import org.ossreviewtoolkit.workbench.composables.ListScreenContent
import org.ossreviewtoolkit.workbench.composables.ListScreenList
import org.ossreviewtoolkit.workbench.composables.Preview
import org.ossreviewtoolkit.workbench.composables.SeverityIcon
import org.ossreviewtoolkit.workbench.model.LicenseFindingWithProvenance
import org.ossreviewtoolkit.workbench.model.ResolutionStatus
import org.ossreviewtoolkit.workbench.model.ResolvedRuleViolation
import org.ossreviewtoolkit.workbench.model.SourceCodeResult

@Composable
@Preview
fun Violations(viewModel: ViolationsViewModel) {
    val stateState = viewModel.state.collectAsState()
    val curationDialogData by viewModel.curationDialogState.collectAsState()
    val curationCount by viewModel.curationCount.collectAsState()

    when (val state = stateState.value) {
        is ViolationsState.Loading -> CircularProgressBox()

        is ViolationsState.Success -> {
            Column {
                if (curationCount > 0) {
                    CurationsSaveBar(curationCount, viewModel)
                }

                ListScreenContent(
                    filterText = state.filter.text,
                    onUpdateFilterText = viewModel::updateTextFilter,
                    list = {
                        ListScreenList(
                            items = state.violations,
                            itemsEmptyText = "No violations found.",
                            item = { ViolationCard(it, viewModel) }
                        )
                    },
                    filterPanel = { showFilterPanel ->
                        ViolationsFilterPanel(
                            visible = showFilterPanel,
                            state = state,
                            onUpdateIdentifierFilter = viewModel::updateIdentifierFilter,
                            onUpdateLicenseFilter = viewModel::updateLicenseFilter,
                            onUpdateLicenseSourceFilter = viewModel::updateLicenseSourceFilter,
                            onUpdateResolutionStatusFilter = viewModel::updateResolutionStatusFilter,
                            onUpdateRuleFilter = viewModel::updateRuleFilter,
                            onUpdateSeverityFilter = viewModel::updateSeverityFilter
                        )
                    }
                )
            }
        }
    }

    curationDialogData?.let { data ->
        CurationDialog(
            data = data,
            onDismiss = viewModel::closeCurationDialog,
            onConfirm = { reason, concludedLicense, comment ->
                viewModel.submitCuration(reason, concludedLicense, comment)
            }
        )
    }
}

@Composable
fun ViolationCard(violation: ResolvedRuleViolation, viewModel: ViolationsViewModel? = null) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 8.dp) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                SeverityIcon(violation.severity, resolved = violation.resolutions.isNotEmpty())
                Text(violation.rule)
                violation.pkg?.let { Text(it.toCoordinates()) }
                Box(modifier = Modifier.weight(1f))
                violation.license?.let { Text(it.toString()) }
                Text("Sources: ${violation.licenseSources.joinToString { it.name }}")
            }

            if (violation.resolutions.isNotEmpty()) Divider()

            violation.resolutions.forEach { resolution ->
                Text("Resolved: ${resolution.reason}", fontWeight = FontWeight.Bold)
                ExpandableText(resolution.comment)
                Divider()
            }

            if (violation.message.isNotBlank()) ExpandableText(violation.message)
            if (violation.howToFix.isNotBlank()) ExpandableMarkdown(violation.howToFix)

            if (viewModel != null && violation.hasDetectedLicense()) {
                LicenseFindingsSection(violation, viewModel)
            }
        }
    }
}

@Composable
private fun LicenseFindingsSection(violation: ResolvedRuleViolation, viewModel: ViolationsViewModel) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Divider()

    if (!expanded) {
        TextButton(onClick = { expanded = true }) {
            Text("Show License Findings")
        }
    } else {
        Text("License Findings", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))

        val findings = remember(violation) { viewModel.getLicenseFindings(violation) }

        if (findings.isEmpty()) {
            Text(
                "No matching license findings found in scan results.",
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                findings.forEach { finding ->
                    LicenseFindingItem(finding, violation.pkg, viewModel)
                }
            }
        }

        TextButton(onClick = { expanded = false }) {
            Text("Hide License Findings")
        }
    }
}

@Composable
private fun LicenseFindingItem(
    finding: LicenseFindingWithProvenance,
    packageId: Identifier?,
    viewModel: ViolationsViewModel
) {
    val location = finding.finding.location
    val findingKey = finding.key()
    val sourceState by viewModel.getSourceCodeState(findingKey).collectAsState()
    val curatedKeys by viewModel.curatedFindingKeys.collectAsState()
    val isCurated = findingKey in curatedKeys

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        backgroundColor = if (isCurated) {
            MaterialTheme.colors.primary.copy(alpha = 0.05f)
        } else {
            MaterialTheme.colors.surface
        }
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LicenseFindingHeader(
                finding = finding,
                location = location,
                isCurated = isCurated,
                sourceState = sourceState,
                onShowSource = { viewModel.loadSourceCode(finding, packageId) },
                onMarkFalsePositive = { viewModel.openCurationDialog(finding, packageId) },
                onRemoveCuration = { viewModel.removeCuration(finding, packageId) }
            )

            when (val state = sourceState) {
                is SourceCodeState.Loaded -> SourceCodeView(
                    result = state.result,
                    detectedLicense = finding.finding.license.toString()
                )
                is SourceCodeState.Error -> {
                    Text(
                        "Error loading source: ${state.message}",
                        color = MaterialTheme.colors.error,
                        fontSize = 12.sp
                    )
                }

                else -> {
                    // Nothing to show.
                }
            }
        }
    }
}

@Composable
private fun LicenseFindingHeader(
    finding: LicenseFindingWithProvenance,
    location: TextLocation,
    isCurated: Boolean,
    sourceState: SourceCodeState,
    onShowSource: () -> Unit,
    onMarkFalsePositive: () -> Unit,
    onRemoveCuration: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "\uD83D\uDCC4 ${location.path}",
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            "lines ${location.startLine}–${location.endLine}",
            fontSize = 12.sp,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
        )

        finding.finding.score?.let { score ->
            Text(
                "score: ${"%.0f".format(score)}",
                fontSize = 12.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        }

        if (isCurated) {
            CuratedBadge(onRemoveCuration)
        } else {
            FalsePositiveButton(onMarkFalsePositive)
        }

        when (sourceState) {
            is SourceCodeState.Idle -> {
                Button(
                    onClick = onShowSource,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colors.primary
                    )
                ) {
                    Text("Show Source", fontSize = 12.sp)
                }
            }

            is SourceCodeState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    strokeWidth = 2.dp
                )
            }

            is SourceCodeState.Loaded, is SourceCodeState.Error -> {
                // Source already shown below.
            }
        }
    }
}

@Composable
private fun FalsePositiveButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.error)
    ) {
        Text("Mark as False Positive", fontSize = 12.sp)
    }
}

@Composable
private fun CuratedBadge(onRemove: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("✓ Curated", fontSize = 12.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
        TextButton(onClick = onRemove) {
            Text("Remove", fontSize = 11.sp, color = MaterialTheme.colors.error)
        }
    }
}

@Composable
private fun CurationsSaveBar(curationCount: Int, viewModel: ViolationsViewModel) {
    var saveMessage by remember { mutableStateOf<String?>(null) }
    val targetDir = remember { viewModel.getPackageConfigDir() }

    Row(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colors.primary.copy(alpha = 0.08f)).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("$curationCount curation(s) pending", fontWeight = FontWeight.Bold)
            Text(
                "Target: $targetDir",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
            )
        }

        saveMessage?.let { message ->
            Text(message, fontSize = 12.sp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f))
        }

        ExportToOrtYmlButton(viewModel) { message -> saveMessage = message }

        Button(onClick = {
            viewModel.saveCurations().fold(
                onSuccess = { count -> saveMessage = "Saved $count file(s) successfully." },
                onFailure = { error -> saveMessage = "Save failed: ${error.message}" }
            )
        }) {
            Text("Save to Package Configurations")
        }
    }
}

@Composable
private fun ExportToOrtYmlButton(viewModel: ViolationsViewModel, onMessage: (String) -> Unit) {
    OutlinedButton(onClick = {
        val fileChooser = javax.swing.JFileChooser().apply {
            dialogTitle = "Export to .ort.yml"
            selectedFile = java.io.File(".ort.yml")
            fileFilter = javax.swing.filechooser.FileNameExtensionFilter("ORT YAML files (*.yml)", "yml")
        }

        if (fileChooser.showSaveDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
            viewModel.exportToOrtYml(fileChooser.selectedFile).fold(
                onSuccess = { count -> onMessage("Exported $count config(s) to ${fileChooser.selectedFile.name}.") },
                onFailure = { error -> onMessage("Export failed: ${error.message}") }
            )
        }
    }) {
        Text("Export to .ort.yml")
    }
}

@Composable
private fun CurationDialog(
    data: CurationDialogData,
    onDismiss: () -> Unit,
    onConfirm: (LicenseFindingCurationReason, String, String) -> Unit
) {
    var reason by remember { mutableStateOf(data.reason) }
    var concludedLicense by remember { mutableStateOf(data.concludedLicense) }
    var comment by remember { mutableStateOf(data.comment) }
    var reasonDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mark as False Positive") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Package: ${data.packageId?.toCoordinates().orEmpty()}", fontSize = 13.sp)
                Text("File: ${data.path}", fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                Text("Lines: ${data.startLine}–${data.endLine}", fontSize = 13.sp)
                Text("Detected License: ${data.detectedLicense}", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Divider()

                Text("Reason:", fontWeight = FontWeight.Bold)

                Box {
                    OutlinedButton(onClick = { reasonDropdownExpanded = true }) {
                        Text(reason.name)
                    }

                    DropdownMenu(
                        expanded = reasonDropdownExpanded,
                        onDismissRequest = { reasonDropdownExpanded = false }
                    ) {
                        LicenseFindingCurationReason.entries.forEach { entry ->
                            DropdownMenuItem(onClick = {
                                reason = entry
                                reasonDropdownExpanded = false
                            }) {
                                Text(entry.name)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = concludedLicense,
                    onValueChange = { concludedLicense = it },
                    label = { Text("Concluded License (NONE = false positive)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = COMMENT_FIELD_MIN_HEIGHT),
                    maxLines = COMMENT_FIELD_MAX_LINES
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason, concludedLicense, comment) },
                enabled = concludedLicense.isNotBlank() && comment.isNotBlank()
            ) {
                Text("Add Curation")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private val COMMENT_FIELD_MIN_HEIGHT = 80.dp
private const val COMMENT_FIELD_MAX_LINES = 5

@Composable
private fun SourceCodeView(result: SourceCodeResult, detectedLicense: String) {
    val highlightColor = MaterialTheme.colors.secondary.copy(alpha = 0.15f)

    SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = MAX_SOURCE_VIEW_HEIGHT)
                .background(Color(0xFF2B2B2B))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Detected: $detectedLicense",
                    color = Color(0xFFCC7832),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            result.lines.forEachIndexed { index, line ->
                val lineNumber = result.firstLineNumber + index
                val isHighlighted = lineNumber in result.findingStartLine..result.findingEndLine

                val annotatedLine = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFF606366))) {
                        append("%4d".format(lineNumber))
                        append("  ")
                    }

                    withStyle(SpanStyle(color = Color(0xFFA9B7C6))) {
                        append(line)
                    }
                }

                Text(
                    text = annotatedLine,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    softWrap = false,
                    modifier = if (isHighlighted) {
                        Modifier.fillMaxWidth().background(highlightColor)
                    } else {
                        Modifier.fillMaxWidth()
                    }
                )
            }
        }
    }
}

private val MAX_SOURCE_VIEW_HEIGHT = 400.dp

private fun ResolvedRuleViolation.hasDetectedLicense(): Boolean =
    license != null && LicenseSource.DETECTED in licenseSources

@Composable
@Preview
private fun ViolationCardPreview() {
    val violation = ResolvedRuleViolation(
        pkg = Identifier("Maven:com.example:package:1.0.0-beta"),
        rule = "RULE_NAME",
        license = SpdxLicenseIdExpression("Apache-2.0"),
        licenseSources = setOf(LicenseSource.DECLARED),
        severity = Severity.WARNING,
        message = "Some long message. ".repeat(20),
        howToFix = """
            # HOW TO FIX
            
            * A
            * **Markdown**
            * *String*
        """.trimIndent(),
        resolutions = listOf(
            RuleViolationResolution(
                "",
                RuleViolationResolutionReason.CANT_FIX_EXCEPTION,
                "Some long comment. ".repeat(20)
            )
        )
    )

    Preview {
        ViolationCard(violation)
    }
}

@Composable
fun ViolationsFilterPanel(
    visible: Boolean,
    state: ViolationsState.Success,
    onUpdateIdentifierFilter: (identifier: Identifier?) -> Unit,
    onUpdateLicenseFilter: (license: SpdxSingleLicenseExpression?) -> Unit,
    onUpdateLicenseSourceFilter: (licenseSource: LicenseSource?) -> Unit,
    onUpdateResolutionStatusFilter: (resolutionStatus: ResolutionStatus?) -> Unit,
    onUpdateRuleFilter: (rule: String?) -> Unit,
    onUpdateSeverityFilter: (severity: Severity?) -> Unit
) {
    FilterPanel(visible = visible) {
        FilterButton(
            data = state.filter.severity,
            label = "Severity",
            onFilterChange = onUpdateSeverityFilter,
            convert = { it.name.titlecase() }
        )

        FilterButton(data = state.filter.license, label = "License", onFilterChange = onUpdateLicenseFilter)

        FilterButton(
            data = state.filter.licenseSource,
            label = "License Source",
            onFilterChange = onUpdateLicenseSourceFilter,
            convert = { it.name.titlecase() }
        )

        FilterButton(data = state.filter.rule, label = "Rule", onFilterChange = onUpdateRuleFilter)

        FilterButton(
            data = state.filter.identifier,
            label = "Package",
            onFilterChange = onUpdateIdentifierFilter,
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
