package org.ossreviewtoolkit.workbench.ui.vulnerabilities

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.workbench.model.FilterData
import org.ossreviewtoolkit.workbench.model.OrtModel
import org.ossreviewtoolkit.workbench.model.ResolutionStatus
import org.ossreviewtoolkit.workbench.model.ResolvedVulnerability
import org.ossreviewtoolkit.workbench.utils.matchResolutionStatus
import org.ossreviewtoolkit.workbench.utils.matchString
import org.ossreviewtoolkit.workbench.utils.matchStringContains
import org.ossreviewtoolkit.workbench.utils.matchValue

class VulnerabilitiesViewModel(private val ortModel: OrtModel) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val vulnerabilities = MutableStateFlow(emptyList<ResolvedVulnerability>())
    private val filter = MutableStateFlow(VulnerabilitiesFilter())

    private val _state = MutableStateFlow(VulnerabilitiesState.INITIAL)
    val state: StateFlow<VulnerabilitiesState> get() = _state

    init {
        scope.launch {
            ortModel.api.collect { api ->
                vulnerabilities.value = api.getVulnerabilities()
            }
        }

        scope.launch { vulnerabilities.collect { initState(it) } }

        scope.launch {
            filter.collect { newFilter ->
                val oldState = state.value
                _state.value = oldState.copy(
                    vulnerabilities = vulnerabilities.value.filter(newFilter::check),
                    textFilter = newFilter.text,
                    advisorFilter = oldState.advisorFilter.copy(selectedItem = newFilter.advisor),
                    identifierFilter = oldState.identifierFilter.copy(selectedItem = newFilter.identifier),
                    resolutionStatusFilter = oldState.resolutionStatusFilter.copy(
                        selectedItem = newFilter.resolutionStatus
                    ),
                    scoringSystemFilter = oldState.scoringSystemFilter.copy(selectedItem = newFilter.scoringSystem),
                    severityFilter = oldState.severityFilter.copy(selectedItem = newFilter.severity),
                )
            }
        }
    }

    private fun initState(vulnerabilities: List<ResolvedVulnerability>) {
        _state.value = VulnerabilitiesState(
            vulnerabilities = vulnerabilities,
            textFilter = "",
            advisorFilter = FilterData(vulnerabilities.mapTo(sortedSetOf()) { it.advisor }.toList()),
            identifierFilter = FilterData(vulnerabilities.mapTo(sortedSetOf()) { it.pkg }.toList()),
            resolutionStatusFilter = FilterData(ResolutionStatus.values().toList()),
            scoringSystemFilter = FilterData(
                vulnerabilities.flatMapTo(sortedSetOf()) { vulnerability ->
                    vulnerability.references.mapNotNull { it.scoringSystem }
                }.toList()
            ),
            severityFilter = FilterData(
                vulnerabilities.flatMapTo(sortedSetOf()) { vulnerability ->
                    vulnerability.references.mapNotNull { it.severity }
                }.toList()
            )
        )
    }

    fun updateTextFilter(text: String) {
        filter.value = filter.value.copy(text = text)
    }

    fun updateAdvisorsFilter(advisor: String?) {
        filter.value = filter.value.copy(advisor = advisor)
    }

    fun updateIdentifiersFilter(identifier: Identifier?) {
        filter.value = filter.value.copy(identifier = identifier)
    }

    fun updateResolutionStatusFilter(resolutionStatus: ResolutionStatus?) {
        filter.value = filter.value.copy(resolutionStatus = resolutionStatus)
    }

    fun updateScoringSystemsFilter(scoringSystem: String?) {
        filter.value = filter.value.copy(scoringSystem = scoringSystem)
    }

    fun updateSeveritiesFilter(severity: String?) {
        filter.value = filter.value.copy(severity = severity)
    }
}

data class VulnerabilitiesFilter(
    val advisor: String? = null,
    val identifier: Identifier? = null,
    val resolutionStatus: ResolutionStatus? = null,
    val scoringSystem: String? = null,
    val severity: String? = null,
    val text: String = ""
) {
    fun check(vulnerability: ResolvedVulnerability) =
        matchString(advisor, vulnerability.advisor)
                && matchValue(identifier, vulnerability.pkg)
                && matchResolutionStatus(resolutionStatus, vulnerability.resolutions)
                && matchString(scoringSystem, vulnerability.references.mapNotNull { it.scoringSystem })
                && matchString(severity, vulnerability.references.mapNotNull { it.severity })
                && matchStringContains(text, vulnerability.pkg.toCoordinates(), vulnerability.id, vulnerability.advisor)
}
