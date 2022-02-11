package org.ossreviewtoolkit.workbench.ui.summary

import java.io.File

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

import org.ossreviewtoolkit.model.OrtIssue
import org.ossreviewtoolkit.model.OrtResult
import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.reporter.reporters.evaluatedmodel.IssueStatistics
import org.ossreviewtoolkit.workbench.model.OrtModel

class SummaryViewModel(private val ortModel: OrtModel = OrtModel.INSTANCE) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _state = MutableStateFlow(SummaryState())
    val state: StateFlow<SummaryState> get() = _state

    init {
        scope.launch {
            combine(
                ortModel.ortResultFile.map { it.toResultFileInfo() },
                ortModel.api.map { IssueStats(it.result) /* TODO: Take resolutions into account. */ },
                ortModel.api.map { DependencyStats(it.result) /* TODO: Take resolutions into account. */ }
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

    constructor(result: OrtResult) : this(
        totalIssues = result.collectIssues().values.flatten().toStats(),
        analyzerIssues = result.analyzer?.result?.collectIssues()?.values?.flatten()?.toStats() ?: EMPTY_STATS,
        advisorIssues = result.advisor?.results?.collectIssues()?.values?.flatten()?.toStats() ?: EMPTY_STATS,
        scannerIssues = result.scanner?.results?.collectIssues()?.values?.flatten()?.toStats() ?: EMPTY_STATS
    )
}

val EMPTY_STATS = IssueStatistics(0, 0, 0)

private fun Collection<OrtIssue>.toStats(): IssueStatistics {
    val grouped = groupBy { it.severity }

    return IssueStatistics(
        errors = grouped[Severity.ERROR]?.size ?: 0,
        warnings = grouped[Severity.WARNING]?.size ?: 0,
        hints = grouped[Severity.HINT]?.size ?: 0
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

    constructor(result: OrtResult) : this(
        projectsTotal = result.getProjects().size,
        projectsByPackageManager = result.getProjects().groupBy { it.id.type }.mapValues { it.value.size },
        dependenciesTotal = result.getPackages().size,
        dependenciesByPackageManager = result.getProjects().groupBy { it.id.type }
            .mapValues { it.value.flatMapTo(mutableSetOf()) { project -> result.collectDependencies(project.id) }.size }
    )
}
