package org.ossreviewtoolkit.workbench.model

import java.time.Instant

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.Issue
import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.model.config.IssueResolution

data class ResolvedIssue(
    val id: Identifier,
    val tool: Tool,
    val resolutions: List<IssueResolution>,
    val timestamp: Instant,
    val source: String,
    val message: String,
    val severity: Severity = Severity.ERROR
) {
    constructor(id: Identifier, tool: Tool, resolutions: List<IssueResolution>, issue: Issue) : this(
        id,
        tool,
        resolutions,
        issue.timestamp,
        issue.source,
        issue.message,
        issue.severity
    )
}
