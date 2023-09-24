package org.ossreviewtoolkit.workbench.ui.summary

sealed interface SummaryState {
    data object Loading : SummaryState

    data class Success(
        val resultFileInfo: ResultFileInfo = ResultFileInfo.EMPTY,
        val issueStats: IssueStats = IssueStats.EMPTY,
        val dependencyStats: DependencyStats = DependencyStats.EMPTY
    ) : SummaryState
}
