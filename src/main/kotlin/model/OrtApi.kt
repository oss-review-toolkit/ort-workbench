package org.ossreviewtoolkit.workbench.model

import org.ossreviewtoolkit.model.AdvisorResult
import org.ossreviewtoolkit.model.AdvisorRun
import org.ossreviewtoolkit.model.AnalyzerRun
import org.ossreviewtoolkit.model.ArtifactProvenance
import org.ossreviewtoolkit.model.CuratedPackage
import org.ossreviewtoolkit.model.EvaluatorRun
import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.Issue
import org.ossreviewtoolkit.model.LicenseSource
import org.ossreviewtoolkit.model.OrtResult
import org.ossreviewtoolkit.model.Package
import org.ossreviewtoolkit.model.Project
import org.ossreviewtoolkit.model.Repository
import org.ossreviewtoolkit.model.RepositoryProvenance
import org.ossreviewtoolkit.model.RuleViolation
import org.ossreviewtoolkit.model.ScanResult
import org.ossreviewtoolkit.model.ScannerDetails
import org.ossreviewtoolkit.model.ScannerRun
import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.model.config.CopyrightGarbage
import org.ossreviewtoolkit.model.config.OrtConfiguration
import org.ossreviewtoolkit.model.config.createFileArchiver
import org.ossreviewtoolkit.model.licenses.DefaultLicenseInfoProvider
import org.ossreviewtoolkit.model.licenses.LicenseInfoProvider
import org.ossreviewtoolkit.model.licenses.LicenseInfoResolver
import org.ossreviewtoolkit.model.licenses.ResolvedLicenseInfo
import org.ossreviewtoolkit.model.utils.DefaultResolutionProvider
import org.ossreviewtoolkit.model.utils.FileArchiver
import org.ossreviewtoolkit.model.utils.ResolutionProvider
import org.ossreviewtoolkit.model.utils.createLicenseInfoResolver
import org.ossreviewtoolkit.plugins.packageconfigurationproviders.api.PackageConfigurationProvider
import org.ossreviewtoolkit.reporter.IssueStatistics
import org.ossreviewtoolkit.workbench.utils.getOrZero

class OrtApi(
    private val result: OrtResult,
    private val config: OrtConfiguration,
    private val copyrightGarbage: CopyrightGarbage,
    private val fileArchiver: FileArchiver?,
    private val licenseInfoProvider: LicenseInfoProvider,
    private val licenseInfoResolver: LicenseInfoResolver,
    private val packageConfigurationProvider: PackageConfigurationProvider?,
    private val resolutionProvider: ResolutionProvider
) {
    companion object {
        val EMPTY by lazy {
            val result = OrtResult.EMPTY
            val copyrightGarbage = CopyrightGarbage()
            val config = OrtConfiguration()
            val fileArchiver = config.scanner.archive.createFileArchiver()
            val packageConfigurationProvider = null
            val licenseInfoProvider = DefaultLicenseInfoProvider(result)

            OrtApi(
                result,
                config,
                copyrightGarbage,
                fileArchiver,
                licenseInfoProvider,
                result.createLicenseInfoResolver(
                    copyrightGarbage,
                    config.addAuthorsToCopyrights,
                    fileArchiver
                ),
                packageConfigurationProvider,
                DefaultResolutionProvider()
            )
        }
    }

    fun getAdvisorIssues(): Map<Identifier, Set<Issue>> = result.advisor?.getIssues().orEmpty()

    fun getAdvisorIssueStats(): IssueStatistics =
        getAdvisorIssues().values.flatten().toIssueStatistics(config.severeIssueThreshold)

    fun getAdvisorRun(): AdvisorRun? = result.advisor

    fun getAnalyzerIssues(): Map<Identifier, Set<Issue>> = result.analyzer?.result?.getAllIssues().orEmpty()

    fun getAnalyzerIssueStats(): IssueStatistics =
        getAnalyzerIssues().values.flatten().toIssueStatistics(config.severeIssueThreshold)

    fun getAdvisorStats(): AdvisorStats =
        result.advisor?.let { advisorRun ->
            val providers = advisorRun.results.values.flatten().map { it.advisor.name }

            AdvisorStats(
                adviceProviderStats = providers.associateWith { provider ->
                    val resultsForProvider = advisorRun.results
                        .filter { (_, results) -> results.any { it.advisor.name == provider } }
                        .mapValues { (_, results) -> results.single { it.advisor.name == provider } }

                    AdviceProviderStats(
                        requestedPackageCount = resultsForProvider.size,
                        packageWithVulnerabilityCount = resultsForProvider.values
                            .count { it.vulnerabilities.isNotEmpty() },
                        totalVulnerabilityCount = resultsForProvider.values.sumOf { it.vulnerabilities.size }
                    )
                }
            )
        } ?: AdvisorStats(emptyMap())

    fun getAnalyzerRun(): AnalyzerRun? = result.analyzer

    fun getCuratedPackage(id: Identifier): CuratedPackage? = result.getPackage(id)

    fun getCuratedPackages(): Set<CuratedPackage> = result.getPackages()

    fun getCuratedPackageOrProject(id: Identifier): CuratedPackage? = result.getPackageOrProject(id)

    fun getUncuratedPackageOrProject(id: Identifier): Package? = result.getUncuratedPackageOrProject(id)

    fun getEvaluatorRun(): EvaluatorRun? = result.evaluator

    fun getEvaluatorStats(): EvaluatorStats =
        result.evaluator?.let { evaluatorRun ->
            EvaluatorStats(
                ruleViolationCount = evaluatorRun.violations.size,
                ruleViolationCountByLicenseSource = evaluatorRun.violations.groupBy { it.licenseSource }.entries
                    .mapNotNull { (key, value) -> key?.let { key to value.size } }.toMap(),
                packageWithRuleViolationCount = evaluatorRun.violations.mapNotNullTo(mutableSetOf()) { it.pkg }.size,
                ruleThatTriggeredViolationCount = evaluatorRun.violations.mapTo(mutableSetOf()) { it.rule }.size
            )
        } ?: EvaluatorStats.EMPTY

    fun getIssues(): Map<Identifier, Set<Issue>> = result.getIssues()

    fun getProject(id: Identifier): Project? = result.getProject(id)

    fun getProjectDependencies(id: Identifier): Set<Identifier> = result.getDependencies(id)

    fun getProjects(): Set<Project> = result.getProjects()

    fun getProjectStats(): ProjectStats {
        val allProjects = result.getProjects().groupBy { it.id.type }
        val dependencyCounts = allProjects.values.flatten().associate { it.id to result.getDependencies(it.id).size }

        return ProjectStats(
            packageManagerStats = allProjects.mapValues { (_, projects) ->
                PackageManagerStats(
                    projectCount = projects.size,
                    dependencyCount = projects.sumOf { dependencyCounts.getOrZero(it.id) }
                )
            }
        )
    }

    fun getProjectAndPackageIdentifiers(): Set<Identifier> = result.getProjectsAndPackages()

    fun getReferences(id: Identifier): List<DependencyReference> =
        result.getProjects().filter { it.contains(id) }.map { project ->
            DependencyReference(
                project = project.id,
                result.isProjectExcluded(project.id),
                scopes = project.scopes.filter { it.contains(id) }.map { scope ->
                    ScopeReference(
                        scope = scope.name,
                        isExcluded = result.getExcludes().isScopeExcluded(scope.name)
                    )
                }
            )
        }

    fun getRepository(): Repository = result.repository

    fun getResolvedIssues(): List<ResolvedIssue> =
        result.analyzer?.result?.getAllIssues().orEmpty().toIssues(Tool.ANALYZER, resolutionProvider) +
                result.advisor?.getIssues().orEmpty().toIssues(Tool.ADVISOR, resolutionProvider) +
                result.scanner?.getAllIssues().orEmpty().toIssues(Tool.SCANNER, resolutionProvider)

    fun getResolvedLicense(id: Identifier): ResolvedLicenseInfo = licenseInfoResolver.resolveLicenseInfo(id)

    fun getRuleViolations(): List<RuleViolation> = result.evaluator?.violations.orEmpty()

    fun getRuleViolationStats() = getRuleViolations().toRuleViolationStatistics(config.severeRuleViolationThreshold)

    fun getScannerIssues(): Map<Identifier, Set<Issue>> = result.scanner?.getAllIssues().orEmpty()

    fun getScannerIssueStats(): IssueStatistics =
        getScannerIssues().values.flatten().toIssueStatistics(config.severeIssueThreshold)

    fun getScannerRun(): ScannerRun? = result.scanner

    fun getScannerStats(): ScannerStats =
        result.scanner?.let { scannerRun ->
            val scannerWrappers = scannerRun.scanResults.map { it.scanner }

            ScannerStats(
                scannerWrapperStats = scannerWrappers.associateWith { details ->
                    val scanResults = scannerRun.scanResults.filter { it.scanner == details }

                    ScannerWrapperStats(
                        scannedPackageCount = scannerRun.getAllScanResults().filter { (_, scanResults) ->
                            scanResults.any { it.scanner == details }
                        }.size,
                        scannedSourceArtifactCount = scanResults.count { it.provenance is ArtifactProvenance },
                        scannedRepositoryCount = scanResults.count { it.provenance is RepositoryProvenance },
                        detectedLicenseCount = scanResults.flatMap { it.summary.licenseFindings }
                            .mapTo(mutableSetOf()) { it.license }.size,
                        detectedCopyrightCount = scanResults.flatMap { it.summary.copyrightFindings }
                            .mapTo(mutableSetOf()) { it.statement }.size
                    )
                }
            )
        } ?: ScannerStats.EMPTY

    fun getScanResults(id: Identifier): List<ScanResult> = result.getScanResultsForId(id)

    fun getViolations(): List<ResolvedRuleViolation> = result.getRuleViolations().toViolations(resolutionProvider)

    fun getVulnerabilities(): List<ResolvedVulnerability> =
        result.getAdvisorResults().toDecoratedVulnerabilities(resolutionProvider)
}

data class ProjectStats(
    val packageManagerStats: Map<String, PackageManagerStats>
) {
    companion object {
        val EMPTY = ProjectStats(emptyMap())
    }
}

data class PackageManagerStats(
    val projectCount: Int,
    val dependencyCount: Int,
)

data class AdvisorStats(
    val adviceProviderStats: Map<String, AdviceProviderStats>
)

data class AdviceProviderStats(
    val requestedPackageCount: Int,
    val packageWithVulnerabilityCount: Int,
    val totalVulnerabilityCount: Int
)

data class ScannerStats(
    val scannerWrapperStats: Map<ScannerDetails, ScannerWrapperStats>
) {
    companion object {
        val EMPTY = ScannerStats(emptyMap())
    }
}

data class ScannerWrapperStats(
    val scannedPackageCount: Int,
    val scannedSourceArtifactCount: Int,
    val scannedRepositoryCount: Int,
    val detectedLicenseCount: Int,
    val detectedCopyrightCount: Int
)

data class EvaluatorStats(
    val ruleViolationCount: Int,
    val ruleViolationCountByLicenseSource: Map<LicenseSource, Int>,
    val packageWithRuleViolationCount: Int,
    val ruleThatTriggeredViolationCount: Int
) {
    companion object {
        val EMPTY = EvaluatorStats(0, emptyMap(), 0, 0)
    }
}

private fun Map<Identifier, List<AdvisorResult>>.toDecoratedVulnerabilities(resolutionProvider: ResolutionProvider) =
    flatMap { (pkg, results) ->
        results.flatMap { result ->
            result.vulnerabilities.map { vulnerability ->
                ResolvedVulnerability(
                    pkg,
                    resolutionProvider.getResolutionsFor(vulnerability),
                    result.advisor.name,
                    vulnerability
                )
            }
        }
    }

private fun Map<Identifier, Set<Issue>>.toIssues(tool: Tool, resolutionProvider: ResolutionProvider) =
    flatMap { (id, issues) ->
        issues.map { issue ->
            ResolvedIssue(id, tool, resolutionProvider.getResolutionsFor(issue), issue)
        }
    }

private fun List<Issue>.toIssueStatistics(severityThreshold: Severity): IssueStatistics {
    val severityCounts = groupingBy { it.severity }.eachCount()

    return IssueStatistics(
        errors = severityCounts.getOrZero(Severity.ERROR),
        warnings = severityCounts.getOrZero(Severity.WARNING),
        hints = severityCounts.getOrZero(Severity.HINT),
        severe = count { it.severity >= severityThreshold }
    )
}

private fun List<RuleViolation>.toRuleViolationStatistics(severityThreshold: Severity): IssueStatistics {
    val severityCounts = groupingBy { it.severity }.eachCount()

    return IssueStatistics(
        errors = severityCounts.getOrZero(Severity.ERROR),
        warnings = severityCounts.getOrZero(Severity.WARNING),
        hints = severityCounts.getOrZero(Severity.HINT),
        severe = count { it.severity >= severityThreshold }
    )
}

private fun List<RuleViolation>.toViolations(resolutionProvider: ResolutionProvider) =
    map { violation -> ResolvedRuleViolation(resolutionProvider.getResolutionsFor(violation), violation) }
