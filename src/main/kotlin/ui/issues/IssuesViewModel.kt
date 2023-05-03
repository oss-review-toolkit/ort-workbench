package org.ossreviewtoolkit.workbench.ui.issues

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.workbench.lifecycle.ViewModel
import org.ossreviewtoolkit.workbench.model.FilterData
import org.ossreviewtoolkit.workbench.model.OrtModel
import org.ossreviewtoolkit.workbench.model.ResolutionStatus
import org.ossreviewtoolkit.workbench.model.ResolvedIssue
import org.ossreviewtoolkit.workbench.model.Tool
import org.ossreviewtoolkit.workbench.utils.matchResolutionStatus
import org.ossreviewtoolkit.workbench.utils.matchString
import org.ossreviewtoolkit.workbench.utils.matchStringContains
import org.ossreviewtoolkit.workbench.utils.matchValue

class IssuesViewModel(private val ortModel: OrtModel) : ViewModel() {
    private val issues = MutableStateFlow(emptyList<ResolvedIssue>())
    private val filter = MutableStateFlow(IssuesFilter())

    private val _state = MutableStateFlow(IssuesState.INITIAL)
    val state: StateFlow<IssuesState> = _state

    init {
        scope.launch { ortModel.api.collect { issues.value = it.getResolvedIssues() } }

        scope.launch { issues.collect { initFilter(it) } }

        scope.launch {
            filter.collect { newFilter ->
                _state.value = _state.value.copy(
                    issues = issues.value.filter(newFilter::check),
                    filter = newFilter
                )
            }
        }
    }

    private fun initFilter(issues: List<ResolvedIssue>) {
        filter.value = IssuesFilter(
            text = "",
            identifier = FilterData(issues.mapTo(sortedSetOf()) { it.id }.toList()),
            resolutionStatus = FilterData(ResolutionStatus.values().toList()),
            severity = FilterData(Severity.values().toList()),
            source = FilterData(issues.mapTo(sortedSetOf()) { it.source }.toList()),
            tool = FilterData(Tool.values().toList())
        )
    }

    fun updateTextFilter(text: String) {
        filter.value = filter.value.copy(text = text)
    }

    fun updateIdentifierFilter(identifier: Identifier?) {
        filter.value = filter.value.copy(identifier = filter.value.identifier.copy(selectedItem = identifier))
    }

    fun updateResolutionStatusFilter(resolutionStatus: ResolutionStatus?) {
        filter.value = filter.value.copy(
            resolutionStatus = filter.value.resolutionStatus.copy(selectedItem = resolutionStatus)
        )
    }

    fun updateSeverityFilter(severity: Severity?) {
        filter.value = filter.value.copy(severity = filter.value.severity.copy(selectedItem = severity))
    }

    fun updateSourceFilter(source: String?) {
        filter.value = filter.value.copy(source = filter.value.source.copy(selectedItem = source))
    }

    fun updateToolFilter(tool: Tool?) {
        filter.value = filter.value.copy(tool = filter.value.tool.copy(selectedItem = tool))
    }
}

data class IssuesFilter(
    val identifier: FilterData<Identifier> = FilterData(),
    val resolutionStatus: FilterData<ResolutionStatus> = FilterData(),
    val severity: FilterData<Severity> = FilterData(),
    val source: FilterData<String> = FilterData(),
    val text: String = "",
    val tool: FilterData<Tool> = FilterData()
) {
    fun check(issue: ResolvedIssue) =
        matchValue(identifier.selectedItem, issue.id)
                && matchResolutionStatus(resolutionStatus.selectedItem, issue.resolutions)
                && matchValue(severity.selectedItem, issue.severity)
                && matchString(source.selectedItem, issue.source)
                && matchStringContains(text, issue.id.toCoordinates(), issue.source, issue.message)
                && matchValue(tool.selectedItem, issue.tool)
}
