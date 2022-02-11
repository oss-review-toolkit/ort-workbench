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
import org.ossreviewtoolkit.model.RuleViolation
import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.model.config.RuleViolationResolution
import org.ossreviewtoolkit.model.utils.ResolutionProvider
import org.ossreviewtoolkit.utils.spdx.SpdxSingleLicenseExpression
import org.ossreviewtoolkit.workbench.model.OrtModel
import org.ossreviewtoolkit.workbench.util.ResolutionStatus
import org.ossreviewtoolkit.workbench.util.SpdxExpressionStringComparator

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
                val violations = api.result.getRuleViolations().toViolations(api.resolutionProvider)
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

data class Violation(
    val pkg: Identifier?,
    val rule: String,
    val license: SpdxSingleLicenseExpression?,
    val licenseSource: LicenseSource?,
    val severity: Severity,
    val message: String,
    val howToFix: String,
    val resolutions: List<RuleViolationResolution>,
) {
    constructor(
        resolutions: List<RuleViolationResolution>,
        violation: RuleViolation
    ) : this(
        violation.pkg,
        violation.rule,
        violation.license,
        violation.licenseSource,
        violation.severity,
        violation.message,
        violation.howToFix,
        resolutions
    )
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
        (identifier == null || violation.pkg == identifier)
                && (license.isEmpty() || violation.license.toString() == license)
                && (licenseSource == null || violation.licenseSource == licenseSource)
                && (resolutionStatus == ResolutionStatus.ALL
                || resolutionStatus == ResolutionStatus.RESOLVED && violation.resolutions.isNotEmpty()
                || resolutionStatus == ResolutionStatus.UNRESOLVED && violation.resolutions.isEmpty())
                && (rule.isEmpty() || violation.rule == rule)
                && (severity == null || violation.severity == severity)
                && (text.isEmpty()
                || violation.pkg?.toCoordinates()?.contains(text) == true
                || violation.rule.contains(text)
                || violation.license?.toString()?.contains(text) == true
                || violation.licenseSource?.name?.contains(text) == true
                || violation.message.contains(text)
                || violation.howToFix.contains(text))
}

private fun List<RuleViolation>.toViolations(resolutionProvider: ResolutionProvider) =
    map { violation -> Violation(resolutionProvider.getRuleViolationResolutionsFor(violation), violation) }
