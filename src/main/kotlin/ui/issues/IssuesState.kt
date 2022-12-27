package org.ossreviewtoolkit.workbench.ui.issues

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.workbench.model.FilterData
import org.ossreviewtoolkit.workbench.model.Issue
import org.ossreviewtoolkit.workbench.model.Tool
import org.ossreviewtoolkit.workbench.util.ResolutionStatus

data class IssuesState(
    val issues: List<Issue>,
    val textFilter: String,
    val identifierFilter: FilterData<Identifier?>,
    val resolutionStatusFilter: FilterData<ResolutionStatus>,
    val severityFilter: FilterData<Severity?>,
    val sourceFilter: FilterData<String?>,
    val toolFilter: FilterData<Tool?>
) {
    companion object {
        val INITIAL = IssuesState(
            issues = emptyList(),
            textFilter = "",
            identifierFilter = FilterData(null, emptyList()),
            resolutionStatusFilter = FilterData(ResolutionStatus.ALL, emptyList()),
            severityFilter = FilterData(null, emptyList()),
            sourceFilter = FilterData(null, emptyList()),
            toolFilter = FilterData(null, emptyList()),
        )
    }
}
