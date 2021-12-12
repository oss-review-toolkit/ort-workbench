package org.ossreviewtoolkit.workbench.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ossreviewtoolkit.model.AdvisorResult
import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.OrtResult
import org.ossreviewtoolkit.model.Vulnerability
import org.ossreviewtoolkit.model.VulnerabilityReference
import org.ossreviewtoolkit.model.config.VulnerabilityResolution
import org.ossreviewtoolkit.model.utils.ResolutionProvider
import org.ossreviewtoolkit.workbench.util.OrtResultApi
import org.ossreviewtoolkit.workbench.util.ResolutionStatus

class VulnerabilitiesState {
    var initialized by mutableStateOf(false)
        private set

    private var resultApi = OrtResultApi(OrtResult.EMPTY)

    private val _vulnerabilities = mutableStateListOf<DecoratedVulnerability>()
    val vulnerabilities: List<DecoratedVulnerability> get() = _vulnerabilities

    private val _filteredVulnerabilities = mutableStateListOf<DecoratedVulnerability>()
    val filteredVulnerabilities: List<DecoratedVulnerability> get() = _filteredVulnerabilities

    private val _advisors = mutableStateListOf<String>()
    val advisors: List<String> get() = _advisors

    private val _identifiers = mutableStateListOf<Identifier>()
    val identifiers: List<Identifier> get() = _identifiers

    private val _scoringSystems = mutableStateListOf<String>()
    val scoringSystems: List<String> get() = _scoringSystems

    private val _severities = mutableStateListOf<String>()
    val severities: List<String> get() = _severities

    var filterAdvisor by mutableStateOf("")
        private set

    var filterIdentifier: Identifier? by mutableStateOf(null)
        private set

    var filterResolutionStatus: ResolutionStatus by mutableStateOf(ResolutionStatus.ALL)
        private set

    var filterScoringSystem by mutableStateOf("")
        private set

    var filterSeverity: String by mutableStateOf("")
        private set

    var filterText by mutableStateOf("")
        private set

    suspend fun initialize(resultApi: OrtResultApi) {
        this.resultApi = resultApi
        _vulnerabilities.clear()
        _vulnerabilities += withContext(Dispatchers.Default) {
            resultApi.result.getAdvisorResults().toDecoratedVulnerabilities(resultApi.resolutionProvider)
        }

        _advisors.clear()
        _advisors += _vulnerabilities.mapTo(sortedSetOf()) { it.advisor }.toList()

        _identifiers.clear()
        _identifiers += _vulnerabilities.mapTo(sortedSetOf()) { it.pkg }.toList()

        _scoringSystems.clear()
        _scoringSystems += _vulnerabilities.flatMapTo(sortedSetOf()) { vulnerability ->
            vulnerability.references.mapNotNull { it.scoringSystem }
        }.toList()

        _severities.clear()
        _severities += _vulnerabilities.flatMapTo(sortedSetOf()) { vulnerability ->
            vulnerability.references.mapNotNull { it.severity }
        }.toList()

        updateFilteredVulnerabilities()
        initialized = true
    }

    fun updateFilterAdvisor(advisor: String) {
        filterAdvisor = advisor
        updateFilteredVulnerabilities()
    }

    fun updateFilterIdentifier(identifier: Identifier?) {
        filterIdentifier = identifier
        updateFilteredVulnerabilities()
    }

    fun updateFilterResolutionStatus(resolutionStatus: ResolutionStatus) {
        filterResolutionStatus = resolutionStatus
        updateFilteredVulnerabilities()
    }

    fun updateFilterScoringSystem(scoringSystem: String) {
        filterScoringSystem = scoringSystem
        updateFilteredVulnerabilities()
    }

    fun updateFilterSeverity(severity: String) {
        filterSeverity = severity
        updateFilteredVulnerabilities()
    }

    fun updateFilterText(filter: String) {
        filterText = filter
        updateFilteredVulnerabilities()
    }

    private fun updateFilteredVulnerabilities() {
        _filteredVulnerabilities.clear()
        _filteredVulnerabilities += _vulnerabilities.filter { vulnerability ->
            (filterAdvisor.isEmpty() || vulnerability.advisor == filterAdvisor)
                    && (filterIdentifier == null || vulnerability.pkg == filterIdentifier)
                    && (filterResolutionStatus == ResolutionStatus.ALL
                    || filterResolutionStatus == ResolutionStatus.RESOLVED && vulnerability.resolutions.isNotEmpty()
                    || filterResolutionStatus == ResolutionStatus.UNRESOLVED && vulnerability.resolutions.isEmpty())
                    && (filterScoringSystem.isEmpty()
                    || vulnerability.references.any { it.scoringSystem == filterScoringSystem })
                    && (filterSeverity.isEmpty() || vulnerability.references.any { it.severity == filterSeverity })
                    && (filterText.isEmpty()
                    || vulnerability.pkg.toCoordinates().contains(filterText)
                    || vulnerability.id.contains(filterText)
                    || vulnerability.advisor.contains(filterText))
        }
    }
}

data class DecoratedVulnerability(
    val pkg: Identifier,
    val resolutions: List<VulnerabilityResolution>,
    val advisor: String,
    val id: String,
    val references: List<VulnerabilityReference>
) {
    constructor(
        pkg: Identifier,
        resolutions: List<VulnerabilityResolution>,
        advisor: String,
        vulnerability: Vulnerability
    ) : this(pkg, resolutions, advisor, vulnerability.id, vulnerability.references)
}

private fun Map<Identifier, List<AdvisorResult>>.toDecoratedVulnerabilities(resolutionProvider: ResolutionProvider) =
    flatMap { (pkg, results) ->
        results.flatMap { result ->
            result.vulnerabilities.map { vulnerability ->
                DecoratedVulnerability(
                    pkg,
                    resolutionProvider.getVulnerabilityResolutionsFor(vulnerability),
                    result.advisor.name,
                    vulnerability
                )
            }
        }
    }
