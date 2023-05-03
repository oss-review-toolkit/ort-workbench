package org.ossreviewtoolkit.workbench.ui.summary

import java.io.File

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

import org.ossreviewtoolkit.model.Issue
import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.reporter.IssueStatistics
import org.ossreviewtoolkit.workbench.lifecycle.ViewModel
import org.ossreviewtoolkit.workbench.model.OrtApi
import org.ossreviewtoolkit.workbench.model.OrtModel

class SummaryViewModel(private val ortModel: OrtModel) : ViewModel() {
    private val _state = MutableStateFlow(SummaryState())
    val state: StateFlow<SummaryState> = _state

    init {
        scope.launch {
            combine(
                ortModel.ortResultFile.map { it.toResultFileInfo() },
                ortModel.api.map { IssueStats(it) /* TODO: Take resolutions into account. */ },
                ortModel.api.map { DependencyStats(it) /* TODO: Take resolutions into account. */ }
            ) { resultFileInfo, issueStats, dependencyStats ->
                SummaryState(
                    resultFileInfo,
                    issueStats,
                    dependencyStats
                )
            }.collect {
                _state.value = it
            }
        }
    }
}

class SummaryState(
    val resultFileInfo: ResultFileInfo = ResultFileInfo.EMPTY,
    val issueStats: IssueStats = IssueStats.EMPTY,
    val dependencyStats: DependencyStats = DependencyStats.EMPTY
)

private fun File?.toResultFileInfo() =
    this?.let { ResultFileInfo(it.absolutePath, it.length()) } ?: ResultFileInfo.EMPTY

data class ResultFileInfo(
    val absolutePath: String,
    val size: Long
) {
    companion object {
        val EMPTY = ResultFileInfo("", 0L)
    }
}

data class IssueStats(
    val totalIssues: IssueStatistics,
    val analyzerIssues: IssueStatistics,
    val advisorIssues: IssueStatistics,
    val scannerIssues: IssueStatistics
) {
    companion object {
        val EMPTY = IssueStats(EMPTY_STATS, EMPTY_STATS, EMPTY_STATS, EMPTY_STATS)
    }

    constructor(api: OrtApi) : this(
        totalIssues = api.getIssues().values.flatten().toStats(),
        analyzerIssues = api.getAnalyzerIssues().values.flatten().toStats(),
        advisorIssues = api.getAdvisorIssues().values.flatten().toStats(),
        scannerIssues = api.getScannerIssues().values.flatten().toStats()
    )
}

val EMPTY_STATS = IssueStatistics(0, 0, 0, 0)

private fun Collection<Issue>.toStats(): IssueStatistics {
    val grouped = groupBy { it.severity }

    return IssueStatistics(
        errors = grouped[Severity.ERROR]?.size ?: 0,
        warnings = grouped[Severity.WARNING]?.size ?: 0,
        hints = grouped[Severity.HINT]?.size ?: 0,
        severe = 0 // TODO: Calculate the number of severe issues.
    )
}

data class DependencyStats(
    val projectsTotal: Int,
    val projectsByPackageManager: Map<String, Int>,
    val dependenciesTotal: Int,
    val dependenciesByPackageManager: Map<String, Int>
) {
    companion object {
        val EMPTY = DependencyStats(
            projectsTotal = 0,
            projectsByPackageManager = emptyMap(),
            dependenciesTotal = 0,
            dependenciesByPackageManager = emptyMap()
        )
    }

    constructor(api: OrtApi) : this(
        projectsTotal = api.getProjects().size,
        projectsByPackageManager = api.getProjects().groupBy { it.id.type }.mapValues { it.value.size },
        dependenciesTotal = api.getCuratedPackages().size,
        dependenciesByPackageManager = api.getProjects().groupBy { it.id.type }
            .mapValues { it.value.flatMapTo(mutableSetOf()) { project -> api.getProjectDependencies(project.id) }.size }
    )
}
