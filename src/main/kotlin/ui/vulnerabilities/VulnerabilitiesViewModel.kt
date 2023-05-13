package org.ossreviewtoolkit.workbench.ui.vulnerabilities

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.workbench.lifecycle.ViewModel
import org.ossreviewtoolkit.workbench.model.FilterData
import org.ossreviewtoolkit.workbench.model.OrtModel
import org.ossreviewtoolkit.workbench.model.ResolutionStatus
import org.ossreviewtoolkit.workbench.model.ResolvedVulnerability
import org.ossreviewtoolkit.workbench.utils.matchResolutionStatus
import org.ossreviewtoolkit.workbench.utils.matchString
import org.ossreviewtoolkit.workbench.utils.matchStringContains
import org.ossreviewtoolkit.workbench.utils.matchValue

class VulnerabilitiesViewModel(private val ortModel: OrtModel) : ViewModel() {
    private val defaultScope = CoroutineScope(Dispatchers.Default)

    private val vulnerabilities = MutableStateFlow(emptyList<ResolvedVulnerability>())
    private val filter = MutableStateFlow(VulnerabilitiesFilter())

    private val _state = MutableStateFlow(VulnerabilitiesState.INITIAL)
    val state: StateFlow<VulnerabilitiesState> = _state

    init {
        defaultScope.launch { ortModel.api.collect { api -> vulnerabilities.value = api.getVulnerabilities() } }

        scope.launch { vulnerabilities.collect { initFilter(it) } }

        scope.launch {
            filter.collect { newFilter ->
                _state.value = _state.value.copy(
                    vulnerabilities = vulnerabilities.value.filter(newFilter::check),
                    filter = newFilter
                )
            }
        }
    }

    private fun initFilter(vulnerabilities: List<ResolvedVulnerability>) {
        filter.value = VulnerabilitiesFilter(
            text = "",
            advisor = FilterData(vulnerabilities.mapTo(sortedSetOf()) { it.advisor }.toList()),
            identifier = FilterData(vulnerabilities.mapTo(sortedSetOf()) { it.pkg }.toList()),
            resolutionStatus = FilterData(ResolutionStatus.values().toList()),
            scoringSystem = FilterData(
                vulnerabilities.flatMapTo(sortedSetOf()) { vulnerability ->
                    vulnerability.references.mapNotNull { it.scoringSystem }
                }.toList()
            ),
            severity = FilterData(
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
        filter.value = filter.value.copy(advisor = filter.value.advisor.copy(selectedItem = advisor))
    }

    fun updateIdentifiersFilter(identifier: Identifier?) {
        filter.value = filter.value.copy(identifier = filter.value.identifier.copy(selectedItem = identifier))
    }

    fun updateResolutionStatusFilter(resolutionStatus: ResolutionStatus?) {
        filter.value = filter.value.copy(
            resolutionStatus = filter.value.resolutionStatus.copy(selectedItem = resolutionStatus)
        )
    }

    fun updateScoringSystemsFilter(scoringSystem: String?) {
        filter.value = filter.value.copy(scoringSystem = filter.value.scoringSystem.copy(selectedItem = scoringSystem))
    }

    fun updateSeveritiesFilter(severity: String?) {
        filter.value = filter.value.copy(severity = filter.value.severity.copy(selectedItem = severity))
    }
}

data class VulnerabilitiesFilter(
    val advisor: FilterData<String> = FilterData(),
    val identifier: FilterData<Identifier> = FilterData(),
    val resolutionStatus: FilterData<ResolutionStatus> = FilterData(),
    val scoringSystem: FilterData<String> = FilterData(),
    val severity: FilterData<String> = FilterData(),
    val text: String = ""
) {
    fun check(vulnerability: ResolvedVulnerability) =
        matchString(advisor.selectedItem, vulnerability.advisor)
                && matchValue(identifier.selectedItem, vulnerability.pkg)
                && matchResolutionStatus(resolutionStatus.selectedItem, vulnerability.resolutions)
                && matchString(scoringSystem.selectedItem, vulnerability.references.mapNotNull { it.scoringSystem })
                && matchString(severity.selectedItem, vulnerability.references.mapNotNull { it.severity })
                && matchStringContains(text, vulnerability.pkg.toCoordinates(), vulnerability.id, vulnerability.advisor)
}
