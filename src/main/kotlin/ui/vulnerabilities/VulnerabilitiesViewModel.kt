package org.ossreviewtoolkit.workbench.ui.vulnerabilities

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.workbench.model.DecoratedVulnerability
import org.ossreviewtoolkit.workbench.model.OrtModel
import org.ossreviewtoolkit.workbench.util.ResolutionStatus
import org.ossreviewtoolkit.workbench.util.matchResolutionStatus
import org.ossreviewtoolkit.workbench.util.matchString
import org.ossreviewtoolkit.workbench.util.matchStringContains
import org.ossreviewtoolkit.workbench.util.matchValue

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
                val vulnerabilities = api.getVulnerabilities()
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

data class VulnerabilitiesFilter(
    val advisor: String = "",
    val identifier: Identifier? = null,
    val resolutionStatus: ResolutionStatus = ResolutionStatus.ALL,
    val scoringSystem: String = "",
    val severity: String = "",
    val text: String = ""
) {
    fun check(vulnerability: DecoratedVulnerability) =
        matchString(advisor, vulnerability.advisor)
                && matchValue(identifier, vulnerability.pkg)
                && matchResolutionStatus(resolutionStatus, vulnerability.resolutions)
                && matchString(scoringSystem, vulnerability.references.mapNotNull { it.scoringSystem })
                && matchString(severity, vulnerability.references.mapNotNull { it.severity })
                && matchStringContains(text, vulnerability.pkg.toCoordinates(), vulnerability.id, vulnerability.advisor)
}
