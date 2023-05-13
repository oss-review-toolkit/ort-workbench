package org.ossreviewtoolkit.workbench.ui.summary

sealed interface SummaryState {
    object Loading : SummaryState

    data class Success(
        val resultFileInfo: ResultFileInfo = ResultFileInfo.EMPTY,
        val issueStats: IssueStats = IssueStats.EMPTY,
        val dependencyStats: DependencyStats = DependencyStats.EMPTY
    ) : SummaryState
}
