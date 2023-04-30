package org.ossreviewtoolkit.workbench.ui.packages

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
import org.ossreviewtoolkit.workbench.lifecycle.ViewModel
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

class PackagesViewModel(private val ortModel: OrtModel) : ViewModel() {
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

        scope.launch { packages.collect { initFilter(it) } }

        scope.launch {
            filter.collect { newFilter ->
                _state.value = _state.value.copy(
                    packages = packages.value.filter(newFilter::check),
                    filter = newFilter
                )
            }
        }
    }

    private fun initFilter(packages: List<PackageInfo>) {
        filter.value = PackagesFilter(
            text = "",
            exclusionStatus = FilterData(ExclusionStatus.values().toList()),
            issueStatus = FilterData(IssueStatus.values().toList()),
            license = FilterData(
                packages.flatMapTo(sortedSetOf(SpdxExpressionStringComparator())) {
                    it.resolvedLicenseInfo.licenses.map { it.license }
                }.toList()
            ),
            namespace = FilterData(packages.mapTo(sortedSetOf()) { it.metadata.id.namespace }.toList()),
            project = FilterData(packages.flatMapTo(sortedSetOf()) { it.references.map { it.project } }.toList()),
            scope = FilterData(
                packages.flatMapTo(sortedSetOf()) {
                    it.references.flatMap { it.scopes.map { it.scope } }
                }.toList()
            ),
            type = FilterData(packages.mapTo(sortedSetOf()) { it.metadata.id.type }.toList()),
            violationStatus = FilterData(ViolationStatus.values().toList()),
            vulnerabilityStatus = FilterData(VulnerabilityStatus.values().toList())
        )
    }

    fun updateTextFilter(text: String) {
        filter.value = filter.value.copy(text = text)
    }

    fun updateExclusionStatusFilter(exclusionStatus: ExclusionStatus?) {
        filter.value = filter.value.copy(
            exclusionStatus = filter.value.exclusionStatus.copy(selectedItem = exclusionStatus)
        )
    }

    fun updateIssueStatusFilter(issueStatus: IssueStatus?) {
        filter.value = filter.value.copy(issueStatus = filter.value.issueStatus.copy(selectedItem = issueStatus))
    }

    fun updateLicenseFilter(license: SpdxSingleLicenseExpression?) {
        filter.value = filter.value.copy(license = filter.value.license.copy(selectedItem = license))
    }

    fun updateNamespaceFilter(namespace: String?) {
        filter.value = filter.value.copy(namespace = filter.value.namespace.copy(selectedItem = namespace))
    }

    fun updateProjectFilter(project: Identifier?) {
        filter.value = filter.value.copy(project = filter.value.project.copy(selectedItem = project))
    }

    fun updateScopeFilter(scope: String?) {
        filter.value = filter.value.copy(scope = filter.value.scope.copy(selectedItem = scope))
    }

    fun updateTypeFilter(type: String?) {
        filter.value = filter.value.copy(type = filter.value.type.copy(selectedItem = type))
    }

    fun updateViolationStatusFilter(violationStatus: ViolationStatus?) {
        filter.value = filter.value.copy(
            violationStatus = filter.value.violationStatus.copy(selectedItem = violationStatus)
        )
    }

    fun updateVulnerabilityStatusFilter(vulnerabilityStatus: VulnerabilityStatus?) {
        filter.value = filter.value.copy(
            vulnerabilityStatus = filter.value.vulnerabilityStatus.copy(selectedItem = vulnerabilityStatus)
        )
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
    val type: FilterData<String> = FilterData(),
    val namespace: FilterData<String> = FilterData(),
    val project: FilterData<Identifier> = FilterData(),
    val scope: FilterData<String> = FilterData(),
    val license: FilterData<SpdxSingleLicenseExpression> = FilterData(),
    val issueStatus: FilterData<IssueStatus> = FilterData(),
    val violationStatus: FilterData<ViolationStatus> = FilterData(),
    val vulnerabilityStatus: FilterData<VulnerabilityStatus> = FilterData(),
    val exclusionStatus: FilterData<ExclusionStatus> = FilterData()
) {
    fun check(pkg: PackageInfo) =
        matchStringContains(text, pkg.metadata.id.toCoordinates())
                && matchString(type.selectedItem, pkg.metadata.id.type)
                && matchString(namespace.selectedItem, pkg.metadata.id.namespace)
                && matchAnyValue(project.selectedItem, pkg.references.map { it.project })
                && matchString(scope.selectedItem, pkg.references.flatMap { it.scopes.map { it.scope } })
                && matchAnyValue(license.selectedItem, pkg.resolvedLicenseInfo.licenses.map { it.license })
                && matchIssueStatus(issueStatus.selectedItem, pkg.issues)
                && matchViolationStatus(violationStatus.selectedItem, pkg.violations)
                && matchVulnerabilityStatus(vulnerabilityStatus.selectedItem, pkg.vulnerabilities)
                && matchExclusionStatus(exclusionStatus.selectedItem, pkg.isExcluded())
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
