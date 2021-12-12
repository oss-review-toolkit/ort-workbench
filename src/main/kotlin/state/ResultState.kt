package org.ossreviewtoolkit.workbench.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.ossreviewtoolkit.model.OrtIssue
import org.ossreviewtoolkit.model.OrtResult
import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.reporter.reporters.evaluatedmodel.IssueStatistics
import org.ossreviewtoolkit.workbench.util.OrtResultApi
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class ResultState {
    var resultApi: OrtResultApi by mutableStateOf(OrtResultApi(OrtResult.EMPTY))
        private set

    var hasAnalyzerResult by mutableStateOf(false)
        private set
    var hasScannerResult by mutableStateOf(false)
        private set
    var hasEvaluatorResult by mutableStateOf(false)
        private set
    var hasAdvisorResult by mutableStateOf(false)
        private set

    // TODO: Show error somehow.
    var error: String? by mutableStateOf(null)
    var path: Path? by mutableStateOf(null)
    var status: ResultStatus by mutableStateOf(ResultStatus.IDLE)

    var resultFileInfo: ResultFileInfo? by mutableStateOf(null)
    var dependencyStats: DependencyStats? by mutableStateOf(null)
    var issueStats: IssueStats? by mutableStateOf(null)
//    var vulnerabilityStats: VulnerabilityStats? by mutableStateOf(null)
//    var ruleViolationStats: RuleViolationStats? by mutableStateOf(null)

    fun setOrtResult(result: OrtResult) {
        resultApi = OrtResultApi(result.withResolvedScopes())

        hasAnalyzerResult = result.analyzer != null
        hasScannerResult = result.scanner != null
        hasEvaluatorResult = result.evaluator != null
        hasAdvisorResult = result.advisor != null

        path?.let {
            resultFileInfo = ResultFileInfo(
                absolutePath = it.absolutePathString(),
                size = it.toFile().length()
            )
        }

        dependencyStats = DependencyStats(result)
        issueStats = IssueStats(result)
    }
}

enum class ResultStatus {
    IDLE, LOADING, PROCESSING, ERROR, FINISHED
}

data class ResultFileInfo(
    val absolutePath: String,
    val size: Long
)

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
    constructor(result: OrtResult) : this(
        projectsTotal = result.getProjects().size,
        projectsByPackageManager = result.getProjects().groupBy { it.id.type }.mapValues { it.value.size },
        dependenciesTotal = result.getPackages().size,
        dependenciesByPackageManager = result.getProjects().groupBy { it.id.type }
            .mapValues { it.value.flatMapTo(mutableSetOf()) { project -> result.collectDependencies(project.id) }.size }
    )
}
