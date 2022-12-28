package org.ossreviewtoolkit.workbench.ui.violations

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.LicenseSource
import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.utils.spdx.SpdxSingleLicenseExpression
import org.ossreviewtoolkit.workbench.model.FilterData
import org.ossreviewtoolkit.workbench.model.ResolutionStatus
import org.ossreviewtoolkit.workbench.model.Violation

data class ViolationsState(
    val violations: List<Violation>,
    val textFilter: String,
    val identifierFilter: FilterData<Identifier>,
    val licenseFilter: FilterData<SpdxSingleLicenseExpression>,
    val licenseSourceFilter: FilterData<LicenseSource>,
    val resolutionStatusFilter: FilterData<ResolutionStatus>,
    val ruleFilter: FilterData<String>,
    val severityFilter: FilterData<Severity>
) {
    companion object {
        val INITIAL = ViolationsState(
            violations = emptyList(),
            textFilter = "",
            identifierFilter = FilterData(),
            licenseFilter = FilterData(),
            licenseSourceFilter = FilterData(),
            resolutionStatusFilter = FilterData(),
            ruleFilter = FilterData(),
            severityFilter = FilterData()
        )
    }
}
