package org.ossreviewtoolkit.workbench.ui.issues

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.workbench.model.FilterData
import org.ossreviewtoolkit.workbench.model.OrtModel
import org.ossreviewtoolkit.workbench.model.ResolutionStatus
import org.ossreviewtoolkit.workbench.model.ResolvedIssue
import org.ossreviewtoolkit.workbench.model.Tool
import org.ossreviewtoolkit.workbench.utils.matchResolutionStatus
import org.ossreviewtoolkit.workbench.utils.matchString
import org.ossreviewtoolkit.workbench.utils.matchStringContains
import org.ossreviewtoolkit.workbench.utils.matchValue

class IssuesViewModel(private val ortModel: OrtModel) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val issues = MutableStateFlow(emptyList<ResolvedIssue>())
    private val filter = MutableStateFlow(IssuesFilter())

    private val _state = MutableStateFlow(IssuesState.INITIAL)
    val state: StateFlow<IssuesState> get() = _state

    init {
        scope.launch { ortModel.api.collect { issues.value = it.getResolvedIssues() } }

        scope.launch { issues.collect { initState(it) } }

        scope.launch {
            filter.collect { newFilter ->
                val oldState = state.value
                _state.value = oldState.copy(
                    issues = issues.value.filter(newFilter::check),
                    textFilter = newFilter.text,
                    identifierFilter = oldState.identifierFilter.copy(selectedItem = newFilter.identifier),
                    resolutionStatusFilter = oldState.resolutionStatusFilter.copy(
                        selectedItem = newFilter.resolutionStatus
                    ),
                    severityFilter = oldState.severityFilter.copy(selectedItem = newFilter.severity),
                    sourceFilter = oldState.sourceFilter.copy(selectedItem = newFilter.source),
                    toolFilter = oldState.toolFilter.copy(selectedItem = newFilter.tool)
                )
            }
        }
    }

    private fun initState(issues: List<ResolvedIssue>) {
        _state.value = IssuesState(
            issues = issues,
            textFilter = "",
            identifierFilter = FilterData(issues.mapTo(sortedSetOf()) { it.id }.toList()),
            resolutionStatusFilter = FilterData(ResolutionStatus.values().toList()),
            severityFilter = FilterData(Severity.values().toList()),
            sourceFilter = FilterData(issues.mapTo(sortedSetOf()) { it.source }.toList()),
            toolFilter = FilterData(Tool.values().toList())
        )
    }

    fun updateTextFilter(text: String) {
        filter.value = filter.value.copy(text = text)
    }

    fun updateIdentifierFilter(identifier: Identifier?) {
        filter.value = filter.value.copy(identifier = identifier)
    }

    fun updateResolutionStatusFilter(resolutionStatus: ResolutionStatus?) {
        filter.value = filter.value.copy(resolutionStatus = resolutionStatus)
    }

    fun updateSeverityFilter(severity: Severity?) {
        filter.value = filter.value.copy(severity = severity)
    }

    fun updateSourceFilter(source: String?) {
        filter.value = filter.value.copy(source = source)
    }

    fun updateToolFilter(tool: Tool?) {
        filter.value = filter.value.copy(tool = tool)
    }
}

data class IssuesFilter(
    val identifier: Identifier? = null,
    val resolutionStatus: ResolutionStatus? = null,
    val severity: Severity? = null,
    val source: String? = null,
    val text: String = "",
    val tool: Tool? = null
) {
    fun check(issue: ResolvedIssue) =
        matchValue(identifier, issue.id)
                && matchResolutionStatus(resolutionStatus, issue.resolutions)
                && matchValue(severity, issue.severity)
                && matchString(source, issue.source)
                && matchStringContains(text, issue.id.toCoordinates(), issue.source, issue.message)
                && matchValue(tool, issue.tool)
}
