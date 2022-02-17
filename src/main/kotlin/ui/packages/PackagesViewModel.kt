package org.ossreviewtoolkit.workbench.ui.packages

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.Package
import org.ossreviewtoolkit.model.PackageCurationResult
import org.ossreviewtoolkit.model.Provenance
import org.ossreviewtoolkit.model.ScanResult
import org.ossreviewtoolkit.model.ScannerDetails
import org.ossreviewtoolkit.model.licenses.ResolvedLicenseInfo
import org.ossreviewtoolkit.utils.spdx.SpdxSingleLicenseExpression
import org.ossreviewtoolkit.workbench.model.DecoratedVulnerability
import org.ossreviewtoolkit.workbench.model.DependencyReference
import org.ossreviewtoolkit.workbench.model.Issue
import org.ossreviewtoolkit.workbench.model.OrtModel
import org.ossreviewtoolkit.workbench.model.Violation
import org.ossreviewtoolkit.workbench.util.SpdxExpressionStringComparator

class PackagesViewModel(private val ortModel: OrtModel = OrtModel.INSTANCE) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _packages = MutableStateFlow(emptyList<PackageInfo>())
    val packages: StateFlow<List<PackageInfo>> get() = _packages

    private val _filteredPackages = MutableStateFlow(emptyList<PackageInfo>())
    val filteredPackages: StateFlow<List<PackageInfo>> get() = _filteredPackages

    private val _types = MutableStateFlow(emptyList<String>())
    val types: StateFlow<List<String>> get() = _types

    private val _namespaces = MutableStateFlow(emptyList<String>())
    val namespaces: StateFlow<List<String>> get() = _namespaces

    private val _projects = MutableStateFlow(emptyList<Identifier>())
    val projects: StateFlow<List<Identifier>> get() = _projects

    private val _scopes = MutableStateFlow(emptyList<String>())
    val scopes: StateFlow<List<String>> get() = _scopes

    private val _licenses = MutableStateFlow(emptyList<SpdxSingleLicenseExpression>())
    val licenses: StateFlow<List<SpdxSingleLicenseExpression>> get() = _licenses

    private val _filter = MutableStateFlow(PackagesFilter())
    val filter: StateFlow<PackagesFilter> get() = _filter

    init {
        scope.launch {
            ortModel.api.collect { api ->
                val projectsAndPackages = (api.result.getProjects().map { it.toPackage().toCuratedPackage() }
                        + api.result.getPackages()).sorted()

                val packages = projectsAndPackages.map { pkg ->
                    val references = api.getReferences(pkg.pkg.id)
                    val issues = api.getIssues().filter { it.id == pkg.pkg.id }
                    val violations = api.getViolations().filter { it.pkg == pkg.pkg.id }
                    val vulnerabilities = api.getVulnerabilities().filter { it.pkg == pkg.pkg.id }
                    val scanResultInfos = api.getScanResults(pkg.pkg.id).map { it.toInfo() }

                    PackageInfo(
                        pkg = pkg.pkg,
                        curations = pkg.curations,
                        resolvedLicenseInfo = api.licenseInfoResolver.resolveLicenseInfo(pkg.pkg.id),
                        references = references,
                        issues = issues,
                        violations = violations,
                        vulnerabilities = vulnerabilities,
                        scanResultInfos = scanResultInfos
                    )
                }

                _packages.value = packages
                // TODO: Check how to do this when declaring the properties.
                _types.value = packages.mapTo(sortedSetOf()) { it.pkg.id.type }.toList()
                _namespaces.value = packages.mapTo(sortedSetOf()) { it.pkg.id.namespace }.toList()
                _projects.value = packages.flatMapTo(sortedSetOf()) { it.references.map { it.project } }.toList()
                _scopes.value = packages.flatMapTo(sortedSetOf()) {
                    it.references.flatMap { it.scopes.map { it.scope } }
                }.toList()
                _licenses.value = packages.flatMapTo(sortedSetOf(SpdxExpressionStringComparator())) {
                    it.resolvedLicenseInfo.licenses.map { it.license }
                }.toList()
            }
        }

        scope.launch {
            combine(packages, filter) { packages, filter ->
                packages.filter(filter::check)
            }.collect { _filteredPackages.value = it }
        }
    }

    fun updateFilter(filter: PackagesFilter) {
        _filter.value = filter
    }
}

data class PackageInfo(
    val pkg: Package,
    val curations: List<PackageCurationResult>,
    val resolvedLicenseInfo: ResolvedLicenseInfo,
    val references: List<DependencyReference>,
    val issues: List<Issue>,
    val violations: List<Violation>,
    val vulnerabilities: List<DecoratedVulnerability>,
    val scanResultInfos: List<ScanResultInfo>
) {
    fun isExcluded() = references.all { it.isExcluded || it.scopes.all { it.isExcluded } }
}

data class ScanResultInfo(
    val scanner: ScannerDetails,
    val provenance: Provenance
)

data class PackagesFilter(
    val text: String = "",
    val type: String = "",
    val namespace: String = "",
    val project: Identifier? = null,
    val scope: String = "",
    val license: SpdxSingleLicenseExpression? = null,
    val issueStatus: IssueStatus = IssueStatus.ALL,
    val violationStatus: ViolationStatus = ViolationStatus.ALL,
    val vulnerabilityStatus: VulnerabilityStatus = VulnerabilityStatus.ALL,
    val exclusionStatus: ExclusionStatus = ExclusionStatus.ALL
) {
    fun check(pkg: PackageInfo) =
        (text.isEmpty()
                || pkg.pkg.id.toCoordinates().contains(text))
                && (type.isEmpty() || pkg.pkg.id.type == type)
                && (namespace.isEmpty() || pkg.pkg.id.namespace == namespace)
                && (project == null || pkg.references.any { it.project == project })
                && (scope.isEmpty() || pkg.references.any { it.scopes.any { it.scope == scope } })
                && (license == null || license in pkg.resolvedLicenseInfo.licenses.map { it.license })
                && (issueStatus == IssueStatus.ALL
                || issueStatus == IssueStatus.HAS_ISSUES && pkg.issues.isNotEmpty()
                || issueStatus == IssueStatus.NO_ISSUES && pkg.issues.isEmpty())
                && (violationStatus == ViolationStatus.ALL
                || violationStatus == ViolationStatus.HAS_VIOLATIONS && pkg.violations.isNotEmpty()
                || violationStatus == ViolationStatus.NO_VIOLATIONS && pkg.violations.isEmpty())
                && (vulnerabilityStatus == VulnerabilityStatus.ALL
                || vulnerabilityStatus == VulnerabilityStatus.HAS_VULNERABILITY && pkg.vulnerabilities.isNotEmpty()
                || vulnerabilityStatus == VulnerabilityStatus.NO_VULNERABILITY && pkg.vulnerabilities.isEmpty())
                && (exclusionStatus == ExclusionStatus.ALL
                || exclusionStatus == ExclusionStatus.EXCLUDED && pkg.isExcluded()
                || exclusionStatus == ExclusionStatus.INCLUDED && !pkg.isExcluded())
}

enum class IssueStatus {
    ALL,
    HAS_ISSUES,
    NO_ISSUES
}

enum class ViolationStatus {
    ALL,
    HAS_VIOLATIONS,
    NO_VIOLATIONS
}

enum class VulnerabilityStatus {
    ALL,
    HAS_VULNERABILITY,
    NO_VULNERABILITY
}

enum class ExclusionStatus {
    ALL,
    EXCLUDED,
    INCLUDED
}

private fun ScanResult.toInfo() = ScanResultInfo(scanner, provenance)
