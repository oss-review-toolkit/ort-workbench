package org.ossreviewtoolkit.workbench.ui.violations

import org.ossreviewtoolkit.workbench.model.ResolvedRuleViolation

sealed interface ViolationsState {
    data object Loading : ViolationsState

    data class Success(
        val violations: List<ResolvedRuleViolation>,
        val filter: ViolationsFilter
    ) : ViolationsState
}
