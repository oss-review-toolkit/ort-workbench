package org.ossreviewtoolkit.workbench.ui.vulnerabilities

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

import org.ossreviewtoolkit.model.AdvisorResult
import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.Vulnerability
import org.ossreviewtoolkit.model.VulnerabilityReference
import org.ossreviewtoolkit.model.config.VulnerabilityResolution
import org.ossreviewtoolkit.model.utils.ResolutionProvider
import org.ossreviewtoolkit.workbench.model.OrtModel
import org.ossreviewtoolkit.workbench.util.ResolutionStatus

class VulnerabilitiesViewModel(private val ortModel: OrtModel = OrtModel.INSTANCE) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _vulnerabilities = MutableStateFlow(emptyList<DecoratedVulnerability>())
    val vulnerabilities: StateFlow<List<DecoratedVulnerability>> get() = _vulnerabilities

    private val _filteredVulnerabilities = MutableStateFlow(emptyList<DecoratedVulnerability>())
    val filteredVulnerabilities: StateFlow<List<DecoratedVulnerability>> get() = _filteredVulnerabilities

    private val _advisors = MutableStateFlow(emptyList<String>())
    val advisors: StateFlow<List<String>> get() = _advisors

    private val _identifiers = MutableStateFlow(emptyList<Identifier>())
    val identifiers: StateFlow<List<Identifier>> get() = _identifiers

    private val _scoringSystems = MutableStateFlow(emptyList<String>())
    val scoringSystems: StateFlow<List<String>> get() = _scoringSystems

    private val _severities = MutableStateFlow(emptyList<String>())
    val severities: StateFlow<List<String>> get() = _severities

    private val _filter = MutableStateFlow(VulnerabilitiesFilter())
    val filter: StateFlow<VulnerabilitiesFilter> get() = _filter

    init {
        scope.launch {
            ortModel.api.collect { api ->
                val vulnerabilities = api.result.getAdvisorResults().toDecoratedVulnerabilities(api.resolutionProvider)
                _vulnerabilities.value = vulnerabilities
                // TODO: Check how to do this when declaring `_advisors`,`_identifiers`, `_scoringSystems` and
                //       `_severities`.
                _advisors.value = vulnerabilities.mapTo(sortedSetOf()) { it.advisor }.toList()
                _identifiers.value = vulnerabilities.mapTo(sortedSetOf()) { it.pkg }.toList()
                _scoringSystems.value = vulnerabilities.flatMapTo(sortedSetOf()) { vulnerability ->
                    vulnerability.references.mapNotNull { it.scoringSystem }
                }.toList()
                _severities.value = vulnerabilities.flatMapTo(sortedSetOf()) { vulnerability ->
                    vulnerability.references.mapNotNull { it.severity }
                }.toList()
            }
        }

        scope.launch {
            combine(vulnerabilities, filter) { vulnerabilities, filter ->
                vulnerabilities.filter(filter::check)
            }.collect { _filteredVulnerabilities.value = it }
        }
    }

    fun updateFilter(filter: VulnerabilitiesFilter) {
        _filter.value = filter
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

data class VulnerabilitiesFilter(
    val advisor: String = "",
    val identifier: Identifier? = null,
    val resolutionStatus: ResolutionStatus = ResolutionStatus.ALL,
    val scoringSystem: String = "",
    val severity: String = "",
    val text: String = ""
) {
    fun check(vulnerability: DecoratedVulnerability) =
        (advisor.isEmpty() || vulnerability.advisor == advisor)
                && (identifier == null || vulnerability.pkg == identifier)
                && (resolutionStatus == ResolutionStatus.ALL
                || resolutionStatus == ResolutionStatus.RESOLVED && vulnerability.resolutions.isNotEmpty()
                || resolutionStatus == ResolutionStatus.UNRESOLVED && vulnerability.resolutions.isEmpty())
                && (scoringSystem.isEmpty()
                || vulnerability.references.any { it.scoringSystem == scoringSystem })
                && (severity.isEmpty() || vulnerability.references.any { it.severity == severity })
                && (text.isEmpty()
                || vulnerability.pkg.toCoordinates().contains(text)
                || vulnerability.id.contains(text)
                || vulnerability.advisor.contains(text))
}
