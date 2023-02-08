package org.ossreviewtoolkit.workbench.model

import org.ossreviewtoolkit.model.AdvisorResult
import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.OrtIssue
import org.ossreviewtoolkit.model.OrtResult
import org.ossreviewtoolkit.model.RuleViolation
import org.ossreviewtoolkit.model.ScanResult
import org.ossreviewtoolkit.model.config.CopyrightGarbage
import org.ossreviewtoolkit.model.config.OrtConfiguration
import org.ossreviewtoolkit.model.config.createFileArchiver
import org.ossreviewtoolkit.model.licenses.DefaultLicenseInfoProvider
import org.ossreviewtoolkit.model.licenses.LicenseInfoProvider
import org.ossreviewtoolkit.model.licenses.LicenseInfoResolver
import org.ossreviewtoolkit.model.utils.DefaultResolutionProvider
import org.ossreviewtoolkit.model.utils.FileArchiver
import org.ossreviewtoolkit.model.utils.PackageConfigurationProvider
import org.ossreviewtoolkit.model.utils.ResolutionProvider
import org.ossreviewtoolkit.model.utils.createLicenseInfoResolver

class OrtApi(
    val result: OrtResult,
    val config: OrtConfiguration,
    val copyrightGarbage: CopyrightGarbage,
    val fileArchiver: FileArchiver?,
    val licenseInfoProvider: LicenseInfoProvider,
    val licenseInfoResolver: LicenseInfoResolver,
    val packageConfigurationProvider: PackageConfigurationProvider,
    val resolutionProvider: ResolutionProvider
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

    fun getIssues(): List<ResolvedIssue> =
        result.analyzer?.result?.collectIssues().orEmpty().toIssues(Tool.ANALYZER, resolutionProvider) +
                result.advisor?.results?.collectIssues().orEmpty().toIssues(Tool.ADVISOR, resolutionProvider) +
                result.scanner?.collectIssues().orEmpty().toIssues(Tool.SCANNER, resolutionProvider)

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

    fun getViolations(): List<Violation> = result.getRuleViolations().toViolations(resolutionProvider)

    fun getVulnerabilities(): List<ResolvedVulnerability> =
        result.getAdvisorResults().toDecoratedVulnerabilities(resolutionProvider)

    fun getScanResults(id: Identifier): List<ScanResult> = result.getScanResultsForId(id)
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

private fun Map<Identifier, Set<OrtIssue>>.toIssues(tool: Tool, resolutionProvider: ResolutionProvider) =
    flatMap { (id, issues) ->
        issues.map { issue ->
            ResolvedIssue(id, tool, resolutionProvider.getIssueResolutionsFor(issue), issue)
        }
    }

private fun List<RuleViolation>.toViolations(resolutionProvider: ResolutionProvider) =
    map { violation -> Violation(resolutionProvider.getRuleViolationResolutionsFor(violation), violation) }
