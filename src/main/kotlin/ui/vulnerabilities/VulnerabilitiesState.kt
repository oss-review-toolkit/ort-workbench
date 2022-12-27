package org.ossreviewtoolkit.workbench.ui.vulnerabilities

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.workbench.model.DecoratedVulnerability
import org.ossreviewtoolkit.workbench.model.FilterData
import org.ossreviewtoolkit.workbench.util.ResolutionStatus

data class VulnerabilitiesState(
    val vulnerabilities: List<DecoratedVulnerability>,
    val textFilter: String,
    val advisorFilter: FilterData<String>,
    val scoringSystemFilter: FilterData<String>,
    val severityFilter: FilterData<String>,
    val identifierFilter: FilterData<Identifier>,
    val resolutionStatusFilter: FilterData<ResolutionStatus>
) {
    companion object {
        val INITIAL = VulnerabilitiesState(
            vulnerabilities = emptyList(),
            textFilter = "",
            advisorFilter = FilterData(),
            identifierFilter = FilterData(),
            resolutionStatusFilter = FilterData(),
            scoringSystemFilter = FilterData(),
            severityFilter = FilterData()
        )
    }
}
