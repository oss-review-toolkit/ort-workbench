package org.ossreviewtoolkit.workbench.ui.summary

import java.time.Instant

import kotlin.time.Duration

import org.ossreviewtoolkit.model.VcsInfo
import org.ossreviewtoolkit.reporter.IssueStatistics
import org.ossreviewtoolkit.workbench.model.AdvisorStats
import org.ossreviewtoolkit.workbench.model.EvaluatorStats
import org.ossreviewtoolkit.workbench.model.ProjectStats
import org.ossreviewtoolkit.workbench.model.ScannerStats

sealed interface SummaryState {
    data object Loading : SummaryState

    data class Success(
        val resultFileInfo: ResultFileInfo = ResultFileInfo.EMPTY,
        val analyzerInfo: AnalyzerInfo = AnalyzerInfo.EMPTY,
        val advisorInfo: AdvisorInfo? = null,
        val scannerInfo: ScannerInfo? = null,
        val evaluatorInfo: EvaluatorInfo? = null
    ) : SummaryState
}

data class ResultFileInfo(
    val absolutePath: String,
    val size: Long,
    val timestamp: Instant,
    val vcs: VcsInfo,
    val nestedRepositories: Map<String, VcsInfo>,
    val repositoryConfigurationStats: RepositoryConfigurationStats
) {
    companion object {
        val EMPTY = ResultFileInfo("", 0L, Instant.EPOCH, VcsInfo.EMPTY, emptyMap(), RepositoryConfigurationStats.EMPTY)
    }
}

data class RepositoryConfigurationStats(
    val pathExcludeCount: Int,
    val scopeExcludeCount: Int,
    val issueResolutionCount: Int,
    val vulnerabilityResolutionCount: Int,
    val ruleViolationResolutionCount: Int,
    val packageCurationCount: Int,
    val licenseFindingCurationCount: Int,
    val packageConfigurationCount: Int,
    val licenseChoiceCount: Int
) {
    companion object {
        val EMPTY = RepositoryConfigurationStats(0, 0, 0, 0, 0, 0, 0, 0, 0)
    }
}

interface ToolInfo {
    val startTime: Instant
    val duration: Duration
    val issueStats: IssueStatistics
    val serializedConfig: String?
    val environment: Map<String, String>?
    val environmentVariables: Map<String, String>?
}

data class AnalyzerInfo(
    override val startTime: Instant,
    override val duration: Duration,
    override val issueStats: IssueStatistics,
    override val serializedConfig: String,
    override val environment: Map<String, String>,
    override val environmentVariables: Map<String, String>,
    val projectStats: ProjectStats
) : ToolInfo {
    companion object {
        val EMPTY = AnalyzerInfo(
            startTime = Instant.EPOCH,
            duration = Duration.ZERO,
            projectStats = ProjectStats.EMPTY,
            issueStats = EMPTY_STATS,
            serializedConfig = "",
            environment = emptyMap(),
            environmentVariables = emptyMap()
        )
    }
}

data class AdvisorInfo(
    override val startTime: Instant,
    override val duration: Duration,
    override val issueStats: IssueStatistics,
    override val serializedConfig: String,
    override val environment: Map<String, String>,
    override val environmentVariables: Map<String, String>,
    val advisorStats: AdvisorStats
) : ToolInfo

data class ScannerInfo(
    override val startTime: Instant,
    override val duration: Duration,
    override val issueStats: IssueStatistics,
    override val serializedConfig: String,
    override val environment: Map<String, String>,
    override val environmentVariables: Map<String, String>,
    val scannerStats: ScannerStats
) : ToolInfo

data class EvaluatorInfo(
    override val startTime: Instant,
    override val duration: Duration,
    override val issueStats: IssueStatistics,
    override val serializedConfig: String? = null,
    override val environment: Map<String, String>? = null,
    override val environmentVariables: Map<String, String>? = null,
    val evaluatorStats: EvaluatorStats
) : ToolInfo
