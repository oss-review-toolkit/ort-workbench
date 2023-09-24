package org.ossreviewtoolkit.workbench.ui.issues

import org.ossreviewtoolkit.workbench.model.ResolvedIssue

sealed interface IssuesState {
    data object Loading : IssuesState

    data class Success(
        val issues: List<ResolvedIssue>,
        val filter: IssuesFilter
    ) : IssuesState
}
