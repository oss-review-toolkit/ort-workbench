package org.ossreviewtoolkit.workbench.ui.summary

import java.io.File
import java.time.Duration
import java.time.Instant

import kotlin.time.toKotlinDuration

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

import org.ossreviewtoolkit.model.config.RepositoryConfiguration
import org.ossreviewtoolkit.model.yamlMapper
import org.ossreviewtoolkit.reporter.IssueStatistics
import org.ossreviewtoolkit.utils.ort.Environment
import org.ossreviewtoolkit.workbench.lifecycle.ViewModel
import org.ossreviewtoolkit.workbench.model.OrtApi
import org.ossreviewtoolkit.workbench.model.OrtModel
import org.ossreviewtoolkit.workbench.utils.removeYamlPrefix

class SummaryViewModel(private val ortModel: OrtModel) : ViewModel() {
    private val defaultScope = CoroutineScope(Dispatchers.Default)

    private val _state = MutableStateFlow<SummaryState>(SummaryState.Loading)
    val state: StateFlow<SummaryState> = _state

    init {
        defaultScope.launch {
            combine(ortModel.ortResultFile, ortModel.api) { resultFile, api ->
                SummaryState.Success(
                    resultFileInfo = createResultFileInfo(resultFile, api),
                    analyzerInfo = createAnalyzerInfo(api),
                    advisorInfo = createAdvisorInfo(api),
                    scannerInfo = createScannerInfo(api),
                    evaluatorInfo = createEvaluatorInfo(api)
                )
            }.collect {
                _state.value = it
            }
        }
    }
}

private fun createResultFileInfo(file: File?, api: OrtApi): ResultFileInfo =
    file?.let {
        ResultFileInfo(
            absolutePath = it.absolutePath,
            size = it.length(),
            timestamp = Instant.ofEpochMilli(it.lastModified()),
            vcs = api.getRepository().vcs,
            nestedRepositories = api.getRepository().nestedRepositories,
            repositoryConfigurationStats = api.getRepository().config.toStats()
        )
    } ?: ResultFileInfo.EMPTY

private fun RepositoryConfiguration.toStats() =
    RepositoryConfigurationStats(
        pathExcludeCount = excludes.paths.size,
        scopeExcludeCount = excludes.scopes.size,
        issueResolutionCount = resolutions.issues.size,
        vulnerabilityResolutionCount = resolutions.vulnerabilities.size,
        ruleViolationResolutionCount = resolutions.ruleViolations.size,
        packageCurationCount = curations.packages.size,
        licenseFindingCurationCount = curations.licenseFindings.size,
        packageConfigurationCount = packageConfigurations.size,
        licenseChoiceCount = licenseChoices.packageLicenseChoices.size + licenseChoices.repositoryLicenseChoices.size
    )

private fun createAnalyzerInfo(api: OrtApi): AnalyzerInfo =
    api.getAnalyzerRun()?.let { analyzerRun ->
        AnalyzerInfo(
            startTime = analyzerRun.startTime,
            duration = Duration.between(analyzerRun.startTime, analyzerRun.endTime).toKotlinDuration(),
            issueStats = api.getAnalyzerIssueStats(),
            serializedConfig = yamlMapper.writeValueAsString(analyzerRun.config).removeYamlPrefix(),
            environment = analyzerRun.environment.toMap(),
            environmentVariables = analyzerRun.environment.variables,
            projectStats = api.getProjectStats()
        )
    } ?: AnalyzerInfo.EMPTY

private fun createAdvisorInfo(api: OrtApi): AdvisorInfo? =
    api.getAdvisorRun()?.let { advisorRun ->
        AdvisorInfo(
            startTime = advisorRun.startTime,
            duration = Duration.between(advisorRun.startTime, advisorRun.endTime).toKotlinDuration(),
            issueStats = api.getAdvisorIssueStats(),
            serializedConfig = yamlMapper.writeValueAsString(advisorRun.config).removeYamlPrefix(),
            environment = advisorRun.environment.toMap(),
            environmentVariables = advisorRun.environment.variables,
            advisorStats = api.getAdvisorStats()
        )
    }

private fun createScannerInfo(api: OrtApi): ScannerInfo? =
    api.getScannerRun()?.let { scannerRun ->
        ScannerInfo(
            startTime = scannerRun.startTime,
            duration = Duration.between(scannerRun.startTime, scannerRun.endTime).toKotlinDuration(),
            issueStats = api.getScannerIssueStats(),
            serializedConfig = yamlMapper.writeValueAsString(scannerRun.config).removeYamlPrefix(),
            environment = scannerRun.environment.toMap(),
            environmentVariables = scannerRun.environment.variables,
            scannerStats = api.getScannerStats()
        )
    }

private fun createEvaluatorInfo(api: OrtApi): EvaluatorInfo? =
    api.getEvaluatorRun()?.let { evaluatorRun ->
        EvaluatorInfo(
            startTime = evaluatorRun.startTime,
            duration = Duration.between(evaluatorRun.startTime, evaluatorRun.endTime).toKotlinDuration(),
            issueStats = api.getRuleViolationStats(),
            evaluatorStats = api.getEvaluatorStats()
        )
    }

private fun Environment.toMap() = mapOf(
    "ORT version" to ortVersion,
    "Java version" to javaVersion,
    "OS" to os,
    "Processors" to processors.toString(),
    "Max memory" to maxMemory.toString()
)

val EMPTY_STATS = IssueStatistics(0, 0, 0, 0)
