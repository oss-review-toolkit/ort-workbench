package org.ossreviewtoolkit.workbench.ui.violations

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.LicenseSource
import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.utils.spdx.SpdxSingleLicenseExpression
import org.ossreviewtoolkit.workbench.lifecycle.ViewModel
import org.ossreviewtoolkit.workbench.model.FilterData
import org.ossreviewtoolkit.workbench.model.OrtModel
import org.ossreviewtoolkit.workbench.model.ResolutionStatus
import org.ossreviewtoolkit.workbench.model.ResolvedRuleViolation
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

data class ViolationsFilter(
    val identifier: FilterData<Identifier> = FilterData(),
    val license: FilterData<SpdxSingleLicenseExpression> = FilterData(),
    val licenseSource: FilterData<LicenseSource> = FilterData(),
    val resolutionStatus: FilterData<ResolutionStatus> = FilterData(),
    val rule: FilterData<String> = FilterData(),
    val severity: FilterData<Severity> = FilterData(),
    val text: String = ""
) {
    fun check(violation: ResolvedRuleViolation) =
        matchValue(identifier.selectedItem, violation.pkg)
                && matchValue(license.selectedItem, violation.license)
                && matchValue(licenseSource.selectedItem, violation.licenseSource)
                && matchResolutionStatus(resolutionStatus.selectedItem, violation.resolutions)
                && matchString(rule.selectedItem, violation.rule)
                && matchValue(severity.selectedItem, violation.severity)
                && matchStringContains(
            text,
            listOfNotNull(
                violation.pkg?.toCoordinates(),
                violation.rule,
                violation.license?.toString(),
                violation.licenseSource?.name,
                violation.message,
                violation.howToFix
            )
        )

    @OptIn(ExperimentalStdlibApi::class)
    fun updateOptions(
        identifiers: List<Identifier>,
        licenses: List<SpdxSingleLicenseExpression>,
        rules: List<String>
    ) = ViolationsFilter(
        identifier = identifier.updateOptions(identifiers),
        license = license.updateOptions(licenses),
        licenseSource = licenseSource.updateOptions(LicenseSource.entries),
        resolutionStatus = resolutionStatus.updateOptions(ResolutionStatus.entries),
        rule = rule.updateOptions(rules),
        severity = severity.updateOptions(Severity.entries),
        text = text
    )
}
