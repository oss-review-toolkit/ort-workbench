package org.ossreviewtoolkit.workbench.ui.packages

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.Package
import org.ossreviewtoolkit.model.PackageCurationResult
import org.ossreviewtoolkit.model.Provenance
import org.ossreviewtoolkit.model.ScanResult
import org.ossreviewtoolkit.model.ScannerDetails
import org.ossreviewtoolkit.model.licenses.ResolvedLicenseInfo
import org.ossreviewtoolkit.utils.spdx.SpdxSingleLicenseExpression
import org.ossreviewtoolkit.workbench.model.DependencyReference
import org.ossreviewtoolkit.workbench.model.FilterData
import org.ossreviewtoolkit.workbench.model.OrtModel
import org.ossreviewtoolkit.workbench.model.ResolvedIssue
import org.ossreviewtoolkit.workbench.model.ResolvedRuleViolation
import org.ossreviewtoolkit.workbench.model.ResolvedVulnerability
import org.ossreviewtoolkit.workbench.utils.SpdxExpressionStringComparator
import org.ossreviewtoolkit.workbench.utils.matchAnyValue
import org.ossreviewtoolkit.workbench.utils.matchExclusionStatus
import org.ossreviewtoolkit.workbench.utils.matchIssueStatus
import org.ossreviewtoolkit.workbench.utils.matchString
import org.ossreviewtoolkit.workbench.utils.matchStringContains
import org.ossreviewtoolkit.workbench.utils.matchViolationStatus
import org.ossreviewtoolkit.workbench.utils.matchVulnerabilityStatus

class PackagesViewModel(private val ortModel: OrtModel = OrtModel.INSTANCE) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val packages = MutableStateFlow(emptyList<PackageInfo>())
    private val filter = MutableStateFlow(PackagesFilter())

    private val _state = MutableStateFlow(PackagesState.INITIAL)
    val state: StateFlow<PackagesState> get() = _state

    init {
        scope.launch {
            ortModel.api.collect { api ->
                val projectPackages = api.getProjects().mapTo(mutableSetOf()) {
                    it.toPackage().toCuratedPackage()
                }

                val projectsAndPackages = (projectPackages + api.getCuratedPackages()).sortedBy { it.metadata.id }

                packages.value = projectsAndPackages.map { pkg ->
                    val references = api.getReferences(pkg.metadata.id)
                    val issues = api.getResolvedIssues().filter { it.id == pkg.metadata.id }
                    val violations = api.getViolations().filter { it.pkg == pkg.metadata.id }
                    val vulnerabilities = api.getVulnerabilities().filter { it.pkg == pkg.metadata.id }
                    val scanResultInfos = api.getScanResults(pkg.metadata.id).map { it.toInfo() }

                    PackageInfo(
                        metadata = pkg.metadata,
                        curations = pkg.curations,
                        resolvedLicenseInfo = api.getResolvedLicense(pkg.metadata.id),
                        references = references,
                        issues = issues,
                        violations = violations,
                        vulnerabilities = vulnerabilities,
                        scanResultInfos = scanResultInfos
                    )
                }
            }
        }

        scope.launch { packages.collect { initState(it) } }

        scope.launch {
            filter.collect { newFilter ->
                val oldState = state.value
                _state.value = oldState.copy(
                    packages = packages.value.filter(newFilter::check),
                    textFilter = newFilter.text,
                    exclusionStatusFilter = oldState.exclusionStatusFilter.copy(
                        selectedItem = newFilter.exclusionStatus
                    ),
                    issueStatusFilter = oldState.issueStatusFilter.copy(selectedItem = newFilter.issueStatus),
                    licenseFilter = oldState.licenseFilter.copy(selectedItem = newFilter.license),
                    namespaceFilter = oldState.namespaceFilter.copy(selectedItem = newFilter.namespace),
                    projectFilter = oldState.projectFilter.copy(selectedItem = newFilter.project),
                    scopeFilter = oldState.scopeFilter.copy(selectedItem = newFilter.scope),
                    typeFilter = oldState.typeFilter.copy(selectedItem = newFilter.type),
                    violationStatusFilter = oldState.violationStatusFilter.copy(
                        selectedItem = newFilter.violationStatus
                    ),
                    vulnerabilityStatusFilter = oldState.vulnerabilityStatusFilter.copy(
                        selectedItem = newFilter.vulnerabilityStatus
                    )
                )
            }
        }
    }

    private fun initState(packages: List<PackageInfo>) {
        _state.value = PackagesState(
            packages = packages,
            textFilter = "",
            exclusionStatusFilter = FilterData(ExclusionStatus.values().toList()),
            issueStatusFilter = FilterData(IssueStatus.values().toList()),
            licenseFilter = FilterData(
                packages.flatMapTo(sortedSetOf(SpdxExpressionStringComparator())) {
                    it.resolvedLicenseInfo.licenses.map { it.license }
                }.toList()
            ),
            namespaceFilter = FilterData(packages.mapTo(sortedSetOf()) { it.metadata.id.namespace }.toList()),
            projectFilter = FilterData(packages.flatMapTo(sortedSetOf()) { it.references.map { it.project } }.toList()),
            scopeFilter = FilterData(
                packages.flatMapTo(sortedSetOf()) {
                    it.references.flatMap { it.scopes.map { it.scope } }
                }.toList()
            ),
            typeFilter = FilterData(packages.mapTo(sortedSetOf()) { it.metadata.id.type }.toList()),
            violationStatusFilter = FilterData(ViolationStatus.values().toList()),
            vulnerabilityStatusFilter = FilterData(VulnerabilityStatus.values().toList())
        )
    }

    fun updateTextFilter(text: String) {
        filter.value = filter.value.copy(text = text)
    }

    fun updateExclusionStatusFilter(exclusionStatus: ExclusionStatus?) {
        filter.value = filter.value.copy(exclusionStatus = exclusionStatus)
    }

    fun updateIssueStatusFilter(issueStatus: IssueStatus?) {
        filter.value = filter.value.copy(issueStatus = issueStatus)
    }

    fun updateLicenseFilter(license: SpdxSingleLicenseExpression?) {
        filter.value = filter.value.copy(license = license)
    }

    fun updateNamespaceFilter(namespace: String?) {
        filter.value = filter.value.copy(namespace = namespace)
    }

    fun updateProjectFilter(project: Identifier?) {
        filter.value = filter.value.copy(project = project)
    }

    fun updateScopeFilter(scope: String?) {
        filter.value = filter.value.copy(scope = scope)
    }

    fun updateTypeFilter(type: String?) {
        filter.value = filter.value.copy(type = type)
    }

    fun updateViolationStatusFilter(violationStatus: ViolationStatus?) {
        filter.value = filter.value.copy(violationStatus = violationStatus)
    }

    fun updateVulnerabilityStatusFilter(vulnerabilityStatus: VulnerabilityStatus?) {
        filter.value = filter.value.copy(vulnerabilityStatus = vulnerabilityStatus)
    }
}

data class PackageInfo(
    val metadata: Package,
    val curations: List<PackageCurationResult>,
    val resolvedLicenseInfo: ResolvedLicenseInfo,
    val references: List<DependencyReference>,
    val issues: List<ResolvedIssue>,
    val violations: List<ResolvedRuleViolation>,
    val vulnerabilities: List<ResolvedVulnerability>,
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
    val type: String? = null,
    val namespace: String? = null,
    val project: Identifier? = null,
    val scope: String? = null,
    val license: SpdxSingleLicenseExpression? = null,
    val issueStatus: IssueStatus? = null,
    val violationStatus: ViolationStatus? = null,
    val vulnerabilityStatus: VulnerabilityStatus? = null,
    val exclusionStatus: ExclusionStatus? = null
) {
    fun check(pkg: PackageInfo) =
        matchStringContains(text, pkg.metadata.id.toCoordinates())
                && matchString(type, pkg.metadata.id.type)
                && matchString(namespace, pkg.metadata.id.namespace)
                && matchAnyValue(project, pkg.references.map { it.project })
                && matchString(scope, pkg.references.flatMap { it.scopes.map { it.scope } })
                && matchAnyValue(license, pkg.resolvedLicenseInfo.licenses.map { it.license })
                && matchIssueStatus(issueStatus, pkg.issues)
                && matchViolationStatus(violationStatus, pkg.violations)
                && matchVulnerabilityStatus(vulnerabilityStatus, pkg.vulnerabilities)
                && matchExclusionStatus(exclusionStatus, pkg.isExcluded())
}

enum class IssueStatus {
    HAS_ISSUES,
    NO_ISSUES
}

enum class ViolationStatus {
    HAS_VIOLATIONS,
    NO_VIOLATIONS
}

enum class VulnerabilityStatus {
    HAS_VULNERABILITY,
    NO_VULNERABILITY
}

enum class ExclusionStatus {
    EXCLUDED,
    INCLUDED
}

private fun ScanResult.toInfo() = ScanResultInfo(scanner, provenance)
