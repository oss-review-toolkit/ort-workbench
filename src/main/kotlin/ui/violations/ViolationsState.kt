package org.ossreviewtoolkit.workbench.ui.violations

import org.ossreviewtoolkit.workbench.model.ResolvedRuleViolation

data class ViolationsState(
    val violations: List<ResolvedRuleViolation>,
    val filter: ViolationsFilter
) {
    companion object {
        val INITIAL = ViolationsState(
            violations = emptyList(),
            filter = ViolationsFilter()
        )
    }
}
