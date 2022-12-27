package org.ossreviewtoolkit.workbench.ui.violations

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.LicenseSource
import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.utils.spdx.SpdxSingleLicenseExpression
import org.ossreviewtoolkit.workbench.model.FilterData
import org.ossreviewtoolkit.workbench.model.Violation
import org.ossreviewtoolkit.workbench.util.ResolutionStatus

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
            identifierFilter = FilterData(null, emptyList()),
            licenseFilter = FilterData(null, emptyList()),
            licenseSourceFilter = FilterData(null, emptyList()),
            resolutionStatusFilter = FilterData(null, emptyList()),
            ruleFilter = FilterData(null, emptyList()),
            severityFilter = FilterData(null, emptyList()),
        )
    }
}
