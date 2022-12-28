package org.ossreviewtoolkit.workbench.ui.issues

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.workbench.model.FilterData
import org.ossreviewtoolkit.workbench.model.Issue
import org.ossreviewtoolkit.workbench.model.ResolutionStatus
import org.ossreviewtoolkit.workbench.model.Tool

data class IssuesState(
    val issues: List<Issue>,
    val textFilter: String,
    val identifierFilter: FilterData<Identifier>,
    val resolutionStatusFilter: FilterData<ResolutionStatus>,
    val severityFilter: FilterData<Severity>,
    val sourceFilter: FilterData<String>,
    val toolFilter: FilterData<Tool>
) {
    companion object {
        val INITIAL = IssuesState(
            issues = emptyList(),
            textFilter = "",
            identifierFilter = FilterData(),
            resolutionStatusFilter = FilterData(),
            severityFilter = FilterData(),
            sourceFilter = FilterData(),
            toolFilter = FilterData()
        )
    }
}
