package org.ossreviewtoolkit.workbench.ui.issues

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.workbench.model.Issue
import org.ossreviewtoolkit.workbench.model.OrtModel
import org.ossreviewtoolkit.workbench.model.Tool
import org.ossreviewtoolkit.workbench.util.ResolutionStatus
import org.ossreviewtoolkit.workbench.util.matchResolutionStatus
import org.ossreviewtoolkit.workbench.util.matchString
import org.ossreviewtoolkit.workbench.util.matchStringContains
import org.ossreviewtoolkit.workbench.util.matchValue

class IssuesViewModel(private val ortModel: OrtModel = OrtModel.INSTANCE) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _issues = MutableStateFlow(emptyList<Issue>())
    val issues: StateFlow<List<Issue>> get() = _issues

    private val _filteredIssues = MutableStateFlow(emptyList<Issue>())
    val filteredIssues: StateFlow<List<Issue>> get() = _filteredIssues

    private val _identifiers = MutableStateFlow(emptyList<Identifier>())
    val identifiers: StateFlow<List<Identifier>> get() = _identifiers

    private val _sources = MutableStateFlow(emptyList<String>())
    val sources: StateFlow<List<String>> get() = _sources

    private val _filter = MutableStateFlow(IssuesFilter())
    val filter: StateFlow<IssuesFilter> get() = _filter

    init {
        scope.launch {
            ortModel.api.collect { api ->
                val issues = api.getIssues()

                _issues.value = issues
                // TODO: Check how to do this when declaring `_identifiers` and `_sources`.
                _identifiers.value = issues.mapTo(sortedSetOf()) { it.id }.toList()
                _sources.value = issues.mapTo(sortedSetOf()) { it.source }.toList()
            }
        }

        scope.launch {
            combine(issues, filter) { issues, filter ->
                issues.filter(filter::check)
            }.collect { _filteredIssues.value = it }
        }
    }

    fun updateFilter(filter: IssuesFilter) {
        _filter.value = filter
    }
}

data class IssuesFilter(
    val identifier: Identifier? = null,
    val resolutionStatus: ResolutionStatus = ResolutionStatus.ALL,
    val severity: Severity? = null,
    val source: String = "",
    val text: String = "",
    val tool: Tool? = null
) {
    fun check(issue: Issue) =
        matchValue(identifier, issue.id)
                && matchResolutionStatus(resolutionStatus, issue.resolutions)
                && matchValue(severity, issue.severity)
                && matchString(source, issue.source)
                && matchStringContains(text, issue.id.toCoordinates(), issue.source, issue.message)
                && matchValue(tool, issue.tool)
}
