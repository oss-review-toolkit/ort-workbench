package org.ossreviewtoolkit.workbench.ui.packages

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.utils.spdx.SpdxSingleLicenseExpression
import org.ossreviewtoolkit.workbench.model.FilterData

data class PackagesState(
    val packages: List<PackageInfo>,
    val textFilter: String,
    val exclusionStatusFilter: FilterData<ExclusionStatus>,
    val issueStatusFilter: FilterData<IssueStatus>,
    val licenseFilter: FilterData<SpdxSingleLicenseExpression>,
    val namespaceFilter: FilterData<String>,
    val projectFilter: FilterData<Identifier>,
    val scopeFilter: FilterData<String>,
    val typeFilter: FilterData<String>,
    val violationStatusFilter: FilterData<ViolationStatus>,
    val vulnerabilityStatusFilter: FilterData<VulnerabilityStatus>
) {
    companion object {
        val INITIAL = PackagesState(
            packages = emptyList(),
            textFilter = "",
            exclusionStatusFilter = FilterData(),
            issueStatusFilter = FilterData(),
            licenseFilter = FilterData(),
            namespaceFilter = FilterData(),
            projectFilter = FilterData(),
            scopeFilter = FilterData(),
            typeFilter = FilterData(),
            violationStatusFilter = FilterData(),
            vulnerabilityStatusFilter = FilterData()
        )
    }
}