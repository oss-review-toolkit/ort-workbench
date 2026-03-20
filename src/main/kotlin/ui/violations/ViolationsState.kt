package org.ossreviewtoolkit.workbench.ui.violations

import org.ossreviewtoolkit.workbench.model.ResolvedRuleViolation
import org.ossreviewtoolkit.workbench.model.SourceCodeResult

sealed interface ViolationsState {
    data object Loading : ViolationsState

    data class Success(
        val violations: List<ResolvedRuleViolation>,
        val filter: ViolationsFilter
    ) : ViolationsState
}

sealed interface SourceCodeState {
    data object Idle : SourceCodeState
    data object Loading : SourceCodeState
    data class Loaded(val result: SourceCodeResult) : SourceCodeState
    data class Error(val message: String) : SourceCodeState
}
