package org.ossreviewtoolkit.workbench.ui.issues

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    private val defaultScope = CoroutineScope(Dispatchers.Default)

    private val issues = MutableStateFlow<List<ResolvedIssue>?>(null)
    private val filter = MutableStateFlow(IssuesFilter())

    private val _state = MutableStateFlow<IssuesState>(IssuesState.Loading)
    val state: StateFlow<IssuesState> = _state

    init {
        defaultScope.launch { ortModel.api.collect { issues.value = it.getResolvedIssues() } }

        scope.launch { issues.collect { if (it != null) initFilter(it) } }

        scope.launch {
            combine(filter, issues) { filter, issues ->
                if (issues != null) {
                    IssuesState.Success(
                        issues = issues.filter(filter::check),
                        filter = filter
                    )
                } else {
                    IssuesState.Loading
                }
            }.collect { _state.value = it }
        }
    }

    private fun initFilter(issues: List<ResolvedIssue>) {
        filter.value = filter.value.updateOptions(
            identifiers = issues.mapTo(sortedSetOf()) { it.id }.toList(),
            sources = issues.mapTo(sortedSetOf()) { it.source }.toList()
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
    fun check(issue: ResolvedIssue) = matchValue(identifier.selectedItem, issue.id)
        && matchResolutionStatus(resolutionStatus.selectedItem, issue.resolutions)
        && matchValue(severity.selectedItem, issue.severity)
        && matchString(source.selectedItem, issue.source)
        && matchStringContains(text, issue.id.toCoordinates(), issue.source, issue.message)
        && matchValue(tool.selectedItem, issue.tool)

    @OptIn(ExperimentalStdlibApi::class)
    fun updateOptions(identifiers: List<Identifier>, sources: List<String>) = IssuesFilter(
        identifier = identifier.updateOptions(identifiers),
        resolutionStatus = resolutionStatus.updateOptions(ResolutionStatus.entries),
        severity = severity.updateOptions(Severity.entries),
        source = source.updateOptions(sources),
        text = text,
        tool = tool.updateOptions(Tool.entries)
    )
}
