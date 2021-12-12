package org.ossreviewtoolkit.workbench.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.LicenseSource
import org.ossreviewtoolkit.model.OrtResult
import org.ossreviewtoolkit.model.RuleViolation
import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.model.config.RuleViolationResolution
import org.ossreviewtoolkit.model.utils.ResolutionProvider
import org.ossreviewtoolkit.utils.spdx.SpdxSingleLicenseExpression
import org.ossreviewtoolkit.workbench.util.OrtResultApi
import org.ossreviewtoolkit.workbench.util.ResolutionStatus

class ViolationsState {
    var initialized by mutableStateOf(false)
        private set

    private var resultApi = OrtResultApi(OrtResult.EMPTY)

    private val _violations = mutableStateListOf<Violation>()
    val violations: List<Violation> get() = _violations

    private val _filteredViolations = mutableStateListOf<Violation>()
    val filteredViolations: List<Violation> get() = _filteredViolations

    private val _identifiers = mutableStateListOf<Identifier>()
    val identifiers: List<Identifier> get() = _identifiers

    private val _licenses = mutableStateListOf<SpdxSingleLicenseExpression>()
    val licenses: List<SpdxSingleLicenseExpression> get() = _licenses

    private val _rules = mutableStateListOf<String>()
    val rules: List<String> get() = _rules

    var filterIdentifier: Identifier? by mutableStateOf(null)
        private set

    var filterLicense by mutableStateOf("")
        private set

    var filterLicenseSource: LicenseSource? by mutableStateOf(null)
        private set

    var filterResolutionStatus: ResolutionStatus by mutableStateOf(ResolutionStatus.ALL)
        private set

    var filterRule by mutableStateOf("")
        private set

    var filterSeverity: Severity? by mutableStateOf(null)
        private set

    var filterText by mutableStateOf("")
        private set

    suspend fun initialize(resultApi: OrtResultApi) {
        this.resultApi = resultApi
        _violations.clear()
        _violations += withContext(Dispatchers.Default) {
            resultApi.result.getRuleViolations().toViolations(resultApi.resolutionProvider)
        }

        _identifiers.clear()
        _identifiers += _violations.mapNotNullTo(sortedSetOf()) { it.pkg }.toList()

        _licenses.clear()
        _licenses += _violations.mapNotNullTo(sortedSetOf()) { it.license }.toList()

        _rules.clear()
        _rules += _violations.mapTo(sortedSetOf()) { it.rule }.toList()

        updateFilteredViolations()
        initialized = true
    }

    fun updateFilterIdentifier(identifier: Identifier?) {
        filterIdentifier = identifier
        updateFilteredViolations()
    }

    fun updateFilterLicense(license: String) {
        filterLicense = license
        updateFilteredViolations()
    }

    fun updateFilterLicenseSource(licenseSource: LicenseSource?) {
        filterLicenseSource = licenseSource
        updateFilteredViolations()
    }

    fun updateFilterResolutionStatus(resolutionStatus: ResolutionStatus) {
        filterResolutionStatus = resolutionStatus
        updateFilteredViolations()
    }

    fun updateFilterRule(rule: String) {
        filterRule = rule
        updateFilteredViolations()
    }

    fun updateFilterSeverity(severity: Severity?) {
        filterSeverity = severity
        updateFilteredViolations()
    }

    fun updateFilterText(filter: String) {
        filterText = filter
        updateFilteredViolations()
    }

    private fun updateFilteredViolations() {
        _filteredViolations.clear()
        _filteredViolations += _violations.filter { violation ->
            (filterIdentifier == null || violation.pkg == filterIdentifier)
                    && (filterLicense.isEmpty() || violation.license.toString() == filterLicense)
                    && (filterLicenseSource == null || violation.licenseSource == filterLicenseSource)
                    && (filterResolutionStatus == ResolutionStatus.ALL
                    || filterResolutionStatus == ResolutionStatus.RESOLVED && violation.resolutions.isNotEmpty()
                    || filterResolutionStatus == ResolutionStatus.UNRESOLVED && violation.resolutions.isEmpty())
                    && (filterRule.isEmpty() || violation.rule == filterRule)
                    && (filterSeverity == null || violation.severity == filterSeverity)
                    && (filterText.isEmpty()
                    || violation.pkg?.toCoordinates()?.contains(filterText) == true
                    || violation.rule.contains(filterText)
                    || violation.license?.toString()?.contains(filterText) == true
                    || violation.licenseSource?.name?.contains(filterText) == true
                    || violation.message.contains(filterText)
                    || violation.howToFix.contains(filterText))
        }
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

private fun List<RuleViolation>.toViolations(resolutionProvider: ResolutionProvider) =
    map { violation -> Violation(resolutionProvider.getRuleViolationResolutionsFor(violation), violation) }
