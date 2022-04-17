package org.ossreviewtoolkit.workbench.ui.violations

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.LicenseSource
import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.utils.spdx.SpdxSingleLicenseExpression
import org.ossreviewtoolkit.workbench.model.OrtModel
import org.ossreviewtoolkit.workbench.model.Violation
import org.ossreviewtoolkit.workbench.util.ResolutionStatus
import org.ossreviewtoolkit.workbench.util.SpdxExpressionStringComparator
import org.ossreviewtoolkit.workbench.util.matchResolutionStatus
import org.ossreviewtoolkit.workbench.util.matchString
import org.ossreviewtoolkit.workbench.util.matchStringContains
import org.ossreviewtoolkit.workbench.util.matchValue

class ViolationsViewModel(private val ortModel: OrtModel = OrtModel.INSTANCE) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _violations = MutableStateFlow(emptyList<Violation>())
    val violations: StateFlow<List<Violation>> get() = _violations

    private val _filteredViolations = MutableStateFlow(emptyList<Violation>())
    val filteredViolations: StateFlow<List<Violation>> get() = _filteredViolations

    private val _identifiers = MutableStateFlow(emptyList<Identifier>())
    val identifiers: StateFlow<List<Identifier>> get() = _identifiers

    private val _licenses = MutableStateFlow(emptyList<SpdxSingleLicenseExpression>())
    val licenses: StateFlow<List<SpdxSingleLicenseExpression>> get() = _licenses

    private val _rules = MutableStateFlow(emptyList<String>())
    val rules: StateFlow<List<String>> get() = _rules

    private val _filter = MutableStateFlow(ViolationsFilter())
    val filter: StateFlow<ViolationsFilter> get() = _filter

    init {
        scope.launch {
            ortModel.api.collect { api ->
                val violations = api.getViolations()
                _violations.value = violations
                // TODO: Check how to do this when declaring `_identifiers` and `_licenses` and `_rules`.
                _identifiers.value = violations.mapNotNullTo(sortedSetOf()) { it.pkg }.toList()
                _licenses.value = violations.mapNotNullTo(
                    sortedSetOf(comparator = SpdxExpressionStringComparator())
                ) { it.license }.toList()
                _rules.value = violations.mapTo(sortedSetOf()) { it.rule }.toList()
            }
        }

        scope.launch {
            combine(violations, filter) { violations, filter ->
                violations.filter(filter::check)
            }.collect { _filteredViolations.value = it }
        }
    }

    fun updateFilter(filter: ViolationsFilter) {
        _filter.value = filter
    }
}

data class ViolationsFilter(
    val identifier: Identifier? = null,
    val license: String = "",
    val licenseSource: LicenseSource? = null,
    val resolutionStatus: ResolutionStatus = ResolutionStatus.ALL,
    val rule: String = "",
    val severity: Severity? = null,
    val text: String = ""
) {
    fun check(violation: Violation) =
        matchValue(identifier, violation.pkg)
                && matchString(license, violation.license.toString())
                && matchValue(licenseSource, violation.licenseSource)
                && matchResolutionStatus(resolutionStatus, violation.resolutions)
                && matchString(rule, violation.rule)
                && matchValue(severity, violation.severity)
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
}
