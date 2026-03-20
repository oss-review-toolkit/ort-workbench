package org.ossreviewtoolkit.workbench.ui.violations

import java.io.File

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

import org.ossreviewtoolkit.model.ArtifactProvenance
import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.LicenseSource
import org.ossreviewtoolkit.model.RepositoryProvenance
import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.model.config.LicenseFindingCurationReason
import org.ossreviewtoolkit.utils.ort.ORT_PACKAGE_CONFIGURATIONS_DIRNAME
import org.ossreviewtoolkit.utils.spdx.SpdxSingleLicenseExpression
import org.ossreviewtoolkit.workbench.lifecycle.ViewModel
import org.ossreviewtoolkit.workbench.model.FilterData
import org.ossreviewtoolkit.workbench.model.LicenseFindingWithProvenance
import org.ossreviewtoolkit.workbench.model.OrtModel
import org.ossreviewtoolkit.workbench.model.ResolutionStatus
import org.ossreviewtoolkit.workbench.model.ResolvedRuleViolation
import org.ossreviewtoolkit.workbench.model.createLicenseFindingCuration
import org.ossreviewtoolkit.workbench.utils.SpdxExpressionStringComparator
import org.ossreviewtoolkit.workbench.utils.matchResolutionStatus
import org.ossreviewtoolkit.workbench.utils.matchString
import org.ossreviewtoolkit.workbench.utils.matchStringContains
import org.ossreviewtoolkit.workbench.utils.matchValue

class ViolationsViewModel(private val ortModel: OrtModel) : ViewModel() {
    private val defaultScope = CoroutineScope(Dispatchers.Default)

    private val violations = MutableStateFlow<List<ResolvedRuleViolation>?>(null)
    private val filter = MutableStateFlow(ViolationsFilter())

    private val _state = MutableStateFlow<ViolationsState>(ViolationsState.Loading)
    val state: StateFlow<ViolationsState> = _state

    private val sourceCodeStates = mutableMapOf<String, MutableStateFlow<SourceCodeState>>()

    private val _curationDialogState = MutableStateFlow<CurationDialogData?>(null)
    val curationDialogState: StateFlow<CurationDialogData?> = _curationDialogState

    val curationCount: StateFlow<Int> = ortModel.curationService.curationCount

    private val _curatedFindingKeys = MutableStateFlow(setOf<String>())
    val curatedFindingKeys: StateFlow<Set<String>> = _curatedFindingKeys

    init {
        defaultScope.launch { ortModel.api.collect { violations.value = it.getViolations() } }

        scope.launch { violations.collect { if (it != null) initFilter(it) } }

        scope.launch {
            combine(filter, violations) { filter, violations ->
                if (violations != null) {
                    ViolationsState.Success(
                        violations = violations.filter(filter::check),
                        filter = filter
                    )
                } else {
                    ViolationsState.Loading
                }
            }.collect { _state.value = it }
        }
    }

    fun getLicenseFindings(violation: ResolvedRuleViolation): List<LicenseFindingWithProvenance> =
        ortModel.api.value.getLicenseFindingsForViolation(violation)

    fun getSourceCodeState(findingKey: String): StateFlow<SourceCodeState> =
        sourceCodeStates.getOrPut(findingKey) { MutableStateFlow(SourceCodeState.Idle) }

    fun loadSourceCode(finding: LicenseFindingWithProvenance, packageId: Identifier?) {
        val key = finding.key()
        val stateFlow = sourceCodeStates.getOrPut(key) { MutableStateFlow(SourceCodeState.Idle) }

        if (stateFlow.value is SourceCodeState.Loaded) return

        val id = packageId ?: return

        stateFlow.value = SourceCodeState.Loading

        scope.launch {
            val result = ortModel.sourceCodeService.getSourceLines(
                provenance = finding.provenance,
                packageId = id,
                path = finding.finding.location.path,
                startLine = finding.finding.location.startLine,
                endLine = finding.finding.location.endLine
            )

            stateFlow.value = result.fold(
                onSuccess = { SourceCodeState.Loaded(it) },
                onFailure = { SourceCodeState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun openCurationDialog(finding: LicenseFindingWithProvenance, packageId: Identifier?) {
        _curationDialogState.value = CurationDialogData(
            finding = finding,
            packageId = packageId,
            detectedLicense = finding.finding.license.toString(),
            path = finding.finding.location.path,
            startLine = finding.finding.location.startLine,
            endLine = finding.finding.location.endLine
        )
    }

    fun closeCurationDialog() {
        _curationDialogState.value = null
    }

    fun submitCuration(reason: LicenseFindingCurationReason, concludedLicense: String, comment: String) {
        val dialogData = _curationDialogState.value ?: return
        val finding = dialogData.finding
        val id = dialogData.packageId ?: return

        val curation = createLicenseFindingCuration(
            path = dialogData.path,
            startLine = dialogData.startLine,
            endLine = dialogData.endLine,
            detectedLicense = dialogData.detectedLicense,
            concludedLicense = concludedLicense,
            reason = reason,
            comment = comment
        )

        ortModel.curationService.addCuration(
            id = id,
            provenance = finding.provenance,
            findingKey = finding.key(),
            curation = curation
        )

        _curatedFindingKeys.value = _curatedFindingKeys.value + finding.key()
        _curationDialogState.value = null
    }

    fun removeCuration(finding: LicenseFindingWithProvenance, packageId: Identifier?) {
        val id = packageId ?: return

        ortModel.curationService.removeCuration(
            id = id,
            provenance = finding.provenance,
            findingKey = finding.key()
        )

        _curatedFindingKeys.value = _curatedFindingKeys.value - finding.key()
    }

    fun hasCuration(findingKey: String): Boolean = ortModel.curationService.hasCuration(findingKey)

    fun saveCurations(): Result<Int> {
        val configDir = File(ortModel.settings.value.ortConfigDir)
        val packageConfigDir = configDir.resolve(ORT_PACKAGE_CONFIGURATIONS_DIRNAME)

        return ortModel.curationService.saveToPackageConfigDir(packageConfigDir)
    }

    fun exportToOrtYml(file: File): Result<Int> = ortModel.curationService.exportToOrtYml(file)

    fun getPackageConfigDir(): String {
        val configDir = File(ortModel.settings.value.ortConfigDir)

        return configDir.resolve(ORT_PACKAGE_CONFIGURATIONS_DIRNAME).absolutePath
    }

    private fun initFilter(violations: List<ResolvedRuleViolation>) {
        filter.value = filter.value.updateOptions(
            identifiers = violations.mapNotNullTo(sortedSetOf()) { it.pkg }.toList(),
            licenses = violations.mapNotNullTo(sortedSetOf(SpdxExpressionStringComparator())) {
                it.license
            }.toList(),
            rules = violations.mapTo(sortedSetOf()) { it.rule }.toList()
        )
    }

    fun updateTextFilter(text: String) {
        filter.value = filter.value.copy(text = text)
    }

    fun updateIdentifierFilter(identifier: Identifier?) {
        filter.value = filter.value.copy(identifier = filter.value.identifier.copy(selectedItem = identifier))
    }

    fun updateLicenseFilter(license: SpdxSingleLicenseExpression?) {
        filter.value = filter.value.copy(license = filter.value.license.copy(selectedItem = license))
    }

    fun updateLicenseSourceFilter(licenseSource: LicenseSource?) {
        filter.value = filter.value.copy(licenseSource = filter.value.licenseSource.copy(selectedItem = licenseSource))
    }

    fun updateResolutionStatusFilter(resolutionStatus: ResolutionStatus?) {
        filter.value = filter.value.copy(
            resolutionStatus = filter.value.resolutionStatus.copy(selectedItem = resolutionStatus)
        )
    }

    fun updateRuleFilter(rule: String?) {
        filter.value = filter.value.copy(rule = filter.value.rule.copy(selectedItem = rule))
    }

    fun updateSeverityFilter(severity: Severity?) {
        filter.value = filter.value.copy(severity = filter.value.severity.copy(selectedItem = severity))
    }
}

fun LicenseFindingWithProvenance.key(): String {
    val provenanceKey = when (val p = provenance) {
        is ArtifactProvenance -> p.sourceArtifact.url
        is RepositoryProvenance -> "${p.vcsInfo.url}@${p.resolvedRevision}"
    }

    return "$provenanceKey:${finding.location.path}:${finding.location.startLine}-${finding.location.endLine}"
}

data class ViolationsFilter(
    val identifier: FilterData<Identifier> = FilterData(),
    val license: FilterData<SpdxSingleLicenseExpression> = FilterData(),
    val licenseSource: FilterData<LicenseSource> = FilterData(),
    val resolutionStatus: FilterData<ResolutionStatus> = FilterData(),
    val rule: FilterData<String> = FilterData(),
    val severity: FilterData<Severity> = FilterData(),
    val text: String = ""
) {
    fun check(violation: ResolvedRuleViolation) = matchValue(identifier.selectedItem, violation.pkg)
        && matchValue(license.selectedItem, violation.license)
        && matchValue(licenseSource.selectedItem, violation.licenseSources)
        && matchResolutionStatus(resolutionStatus.selectedItem, violation.resolutions)
        && matchString(rule.selectedItem, violation.rule)
        && matchValue(severity.selectedItem, violation.severity)
        && matchStringContains(
            text,
            listOfNotNull(
                violation.pkg?.toCoordinates(),
                violation.rule,
                violation.license?.toString(),
                violation.message,
                violation.howToFix
            ) + violation.licenseSources.map { it.name }
        )

    @OptIn(ExperimentalStdlibApi::class)
    fun updateOptions(identifiers: List<Identifier>, licenses: List<SpdxSingleLicenseExpression>, rules: List<String>) =
        ViolationsFilter(
            identifier = identifier.updateOptions(identifiers),
            license = license.updateOptions(licenses),
            licenseSource = licenseSource.updateOptions(LicenseSource.entries),
            resolutionStatus = resolutionStatus.updateOptions(ResolutionStatus.entries),
            rule = rule.updateOptions(rules),
            severity = severity.updateOptions(Severity.entries),
            text = text
        )
}
