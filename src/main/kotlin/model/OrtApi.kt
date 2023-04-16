package org.ossreviewtoolkit.workbench.model

import org.ossreviewtoolkit.model.AdvisorResult
import org.ossreviewtoolkit.model.CuratedPackage
import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.Issue
import org.ossreviewtoolkit.model.OrtResult
import org.ossreviewtoolkit.model.Project
import org.ossreviewtoolkit.model.RuleViolation
import org.ossreviewtoolkit.model.ScanResult
import org.ossreviewtoolkit.model.config.CopyrightGarbage
import org.ossreviewtoolkit.model.config.OrtConfiguration
import org.ossreviewtoolkit.model.config.createFileArchiver
import org.ossreviewtoolkit.model.licenses.DefaultLicenseInfoProvider
import org.ossreviewtoolkit.model.licenses.LicenseInfoProvider
import org.ossreviewtoolkit.model.licenses.LicenseInfoResolver
import org.ossreviewtoolkit.model.licenses.ResolvedLicenseInfo
import org.ossreviewtoolkit.model.utils.DefaultResolutionProvider
import org.ossreviewtoolkit.model.utils.FileArchiver
import org.ossreviewtoolkit.model.utils.PackageConfigurationProvider
import org.ossreviewtoolkit.model.utils.ResolutionProvider
import org.ossreviewtoolkit.model.utils.createLicenseInfoResolver

class OrtApi(
    private val result: OrtResult,
    private val config: OrtConfiguration,
    private val copyrightGarbage: CopyrightGarbage,
    private val fileArchiver: FileArchiver?,
    private val licenseInfoProvider: LicenseInfoProvider,
    private val licenseInfoResolver: LicenseInfoResolver,
    private val packageConfigurationProvider: PackageConfigurationProvider,
    private val resolutionProvider: ResolutionProvider
) {
    companion object {
        val EMPTY by lazy {
            val result = OrtResult.EMPTY
            val copyrightGarbage = CopyrightGarbage()
            val config = OrtConfiguration()
            val fileArchiver = config.scanner.archive.createFileArchiver()
            val packageConfigurationProvider = PackageConfigurationProvider.EMPTY
            val licenseInfoProvider = DefaultLicenseInfoProvider(result, packageConfigurationProvider)

            OrtApi(
                result,
                config,
                copyrightGarbage,
                fileArchiver,
                licenseInfoProvider,
                result.createLicenseInfoResolver(
                    packageConfigurationProvider,
                    copyrightGarbage,
                    config.addAuthorsToCopyrights,
                    fileArchiver
                ),
                packageConfigurationProvider,
                DefaultResolutionProvider()
            )
        }
    }

    fun getAdvisorIssues(): Map<Identifier, Set<Issue>> = result.advisor?.results?.collectIssues().orEmpty()

    fun getAnalyzerIssues(): Map<Identifier, Set<Issue>> = result.analyzer?.result?.collectIssues().orEmpty()

    fun getCuratedPackage(id: Identifier): CuratedPackage? = result.getPackage(id)

    fun getCuratedPackages(): Set<CuratedPackage> = result.getPackages()

    fun getCuratedPackageOrProject(id: Identifier): CuratedPackage? = result.getPackageOrProject(id)

    fun getIssues(): Map<Identifier, Set<Issue>> = result.collectIssues()

    fun getProject(id: Identifier): Project? = result.getProject(id)

    fun getProjectDependencies(id: Identifier): Set<Identifier> = result.collectDependencies(id)

    fun getProjects(): Set<Project> = result.getProjects()

    fun getProjectAndPackageIdentifiers(): Set<Identifier> = result.collectProjectsAndPackages()

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

    fun getResolvedIssues(): List<ResolvedIssue> =
        result.analyzer?.result?.collectIssues().orEmpty().toIssues(Tool.ANALYZER, resolutionProvider) +
                result.advisor?.results?.collectIssues().orEmpty().toIssues(Tool.ADVISOR, resolutionProvider) +
                result.scanner?.collectIssues().orEmpty().toIssues(Tool.SCANNER, resolutionProvider)

    fun getResolvedLicense(id: Identifier): ResolvedLicenseInfo = licenseInfoResolver.resolveLicenseInfo(id)

    fun getScannerIssues(): Map<Identifier, Set<Issue>> = result.scanner?.collectIssues().orEmpty()

    fun getScanResults(id: Identifier): List<ScanResult> = result.getScanResultsForId(id)

    fun getViolations(): List<ResolvedRuleViolation> = result.getRuleViolations().toViolations(resolutionProvider)

    fun getVulnerabilities(): List<ResolvedVulnerability> =
        result.getAdvisorResults().toDecoratedVulnerabilities(resolutionProvider)
}

private fun Map<Identifier, List<AdvisorResult>>.toDecoratedVulnerabilities(resolutionProvider: ResolutionProvider) =
    flatMap { (pkg, results) ->
        results.flatMap { result ->
            result.vulnerabilities.map { vulnerability ->
                ResolvedVulnerability(
                    pkg,
                    resolutionProvider.getVulnerabilityResolutionsFor(vulnerability),
                    result.advisor.name,
                    vulnerability
                )
            }
        }
    }

private fun Map<Identifier, Set<Issue>>.toIssues(tool: Tool, resolutionProvider: ResolutionProvider) =
    flatMap { (id, issues) ->
        issues.map { issue ->
            ResolvedIssue(id, tool, resolutionProvider.getIssueResolutionsFor(issue), issue)
        }
    }

private fun List<RuleViolation>.toViolations(resolutionProvider: ResolutionProvider) =
    map { violation -> ResolvedRuleViolation(resolutionProvider.getRuleViolationResolutionsFor(violation), violation) }
