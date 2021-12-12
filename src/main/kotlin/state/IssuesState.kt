package org.ossreviewtoolkit.workbench.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.OrtIssue
import org.ossreviewtoolkit.model.OrtResult
import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.model.config.IssueResolution
import org.ossreviewtoolkit.model.utils.ResolutionProvider
import org.ossreviewtoolkit.workbench.util.OrtResultApi
import org.ossreviewtoolkit.workbench.util.ResolutionStatus
import java.time.Instant

class IssuesState {
    var initialized by mutableStateOf(false)
        private set

    private var resultApi = OrtResultApi(OrtResult.EMPTY)

    private val _issues = mutableStateListOf<Issue>()
    val issues: List<Issue> get() = _issues

    private val _filteredIssues = mutableStateListOf<Issue>()
    val filteredIssues: List<Issue> get() = _filteredIssues

    private val _identifiers = mutableStateListOf<Identifier>()
    val identifiers: List<Identifier> get() = _identifiers

    private val _sources = mutableStateListOf<String>()
    val sources: List<String> get() = _sources

    var filterIdentifier: Identifier? by mutableStateOf(null)
        private set

    var filterResolutionStatus: ResolutionStatus by mutableStateOf(ResolutionStatus.ALL)
        private set

    var filterSeverity: Severity? by mutableStateOf(null)
        private set

    var filterSource by mutableStateOf("")
        private set

    var filterText by mutableStateOf("")
        private set

    var filterTool: Tool? by mutableStateOf(null)
        private set

    suspend fun initialize(resultApi: OrtResultApi) {
        this.resultApi = resultApi
        _issues.clear()
        _issues += withContext(Dispatchers.Default) {
            resultApi.result.analyzer?.result?.collectIssues().orEmpty()
                .toIssues(Tool.ANALYZER, resultApi.resolutionProvider) +
                    resultApi.result.advisor?.results?.collectIssues().orEmpty()
                        .toIssues(Tool.ADVISOR, resultApi.resolutionProvider) +
                    resultApi.result.scanner?.results?.collectIssues().orEmpty()
                        .toIssues(Tool.SCANNER, resultApi.resolutionProvider)
        }

        _identifiers.clear()
        _identifiers += _issues.mapTo(sortedSetOf()) { it.id }.toList()

        _sources.clear()
        _sources += _issues.mapTo(sortedSetOf()) { it.source }.toList()

        updateFilteredIssues()
        initialized = true
    }

    fun updateFilterIdentifier(identifier: Identifier?) {
        filterIdentifier = identifier
        updateFilteredIssues()
    }

    fun updateFilterResolutionStatus(resolutionStatus: ResolutionStatus) {
        filterResolutionStatus = resolutionStatus
        updateFilteredIssues()
    }

    fun updateFilterSeverity(severity: Severity?) {
        filterSeverity = severity
        updateFilteredIssues()
    }

    fun updateFilterSource(source: String) {
        filterSource = source
        updateFilteredIssues()
    }

    fun updateFilterText(filter: String) {
        filterText = filter
        updateFilteredIssues()
    }

    fun updateFilterTool(tool: Tool?) {
        filterTool = tool
        updateFilteredIssues()
    }

    private fun updateFilteredIssues() {
        _filteredIssues.clear()
        _filteredIssues += _issues.filter { issue ->
            (filterIdentifier == null || issue.id == filterIdentifier)
                    && (filterResolutionStatus == ResolutionStatus.ALL
                    || filterResolutionStatus == ResolutionStatus.RESOLVED && issue.resolutions.isNotEmpty()
                    || filterResolutionStatus == ResolutionStatus.UNRESOLVED && issue.resolutions.isEmpty())
                    && (filterSeverity == null || issue.severity == filterSeverity)
                    && (filterSource.isEmpty() || issue.source == filterSource)
                    && (filterText.isEmpty()
                    || issue.id.toCoordinates().contains(filterText)
                    || issue.source.contains(filterText)
                    || issue.message.contains(filterText))
                    && (filterTool == null || issue.tool == filterTool)
        }
    }
}

data class Issue(
    val id: Identifier,
    val tool: Tool,
    val resolutions: List<IssueResolution>,
    val timestamp: Instant,
    val source: String,
    val message: String,
    val severity: Severity = Severity.ERROR
) {
    constructor(id: Identifier, tool: Tool, resolutions: List<IssueResolution>, issue: OrtIssue) : this(
        id,
        tool,
        resolutions,
        issue.timestamp,
        issue.source,
        issue.message,
        issue.severity
    )
}

private fun Map<Identifier, Set<OrtIssue>>.toIssues(tool: Tool, resolutionProvider: ResolutionProvider) =
    flatMap { (id, issues) ->
        issues.map { issue ->
            Issue(id, tool, resolutionProvider.getIssueResolutionsFor(issue), issue)
        }
    }

/**
 * Tools which can add issues to an ORT result.
 */
enum class Tool {
    ANALYZER,
    ADVISOR,
    SCANNER
}
