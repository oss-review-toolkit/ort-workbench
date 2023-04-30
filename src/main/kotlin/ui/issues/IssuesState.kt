package org.ossreviewtoolkit.workbench.ui.issues

import org.ossreviewtoolkit.workbench.model.ResolvedIssue

data class IssuesState(
    val issues: List<ResolvedIssue>,
    val filter: IssuesFilter
) {
    companion object {
        val INITIAL = IssuesState(
            issues = emptyList(),
            filter = IssuesFilter()
        )
    }
}
