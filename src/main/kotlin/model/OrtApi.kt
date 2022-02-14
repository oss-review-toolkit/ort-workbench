package org.ossreviewtoolkit.workbench.model

import org.ossreviewtoolkit.model.AdvisorResult
import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.OrtIssue
import org.ossreviewtoolkit.model.OrtResult
import org.ossreviewtoolkit.model.RuleViolation
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
import org.ossreviewtoolkit.model.utils.SimplePackageConfigurationProvider
import org.ossreviewtoolkit.model.utils.createLicenseInfoResolver

class OrtApi(
    val result: OrtResult,
    val config: OrtConfiguration,
    val copyrightGarbage: CopyrightGarbage,
    val fileArchiver: FileArchiver,
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
            val packageConfigurationProvider = SimplePackageConfigurationProvider.EMPTY
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

    fun getIssues(): List<Issue> =
        result.analyzer?.result?.collectIssues().orEmpty().toIssues(Tool.ANALYZER, resolutionProvider) +
                result.advisor?.results?.collectIssues().orEmpty().toIssues(Tool.ADVISOR, resolutionProvider) +
                result.scanner?.results?.collectIssues().orEmpty().toIssues(Tool.SCANNER, resolutionProvider)

    fun getViolations(): List<Violation> = result.getRuleViolations().toViolations(resolutionProvider)

    fun getVulnerabilities(): List<DecoratedVulnerability> =
        result.getAdvisorResults().toDecoratedVulnerabilities(resolutionProvider)
}

private fun Map<Identifier, List<AdvisorResult>>.toDecoratedVulnerabilities(resolutionProvider: ResolutionProvider) =
    flatMap { (pkg, results) ->
        results.flatMap { result ->
            result.vulnerabilities.map { vulnerability ->
                DecoratedVulnerability(
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
            Issue(id, tool, resolutionProvider.getIssueResolutionsFor(issue), issue)
        }
    }

private fun List<RuleViolation>.toViolations(resolutionProvider: ResolutionProvider) =
    map { violation -> Violation(resolutionProvider.getRuleViolationResolutionsFor(violation), violation) }
