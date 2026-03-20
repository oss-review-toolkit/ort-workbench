package org.ossreviewtoolkit.workbench.ui.violations

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.config.LicenseFindingCurationReason
import org.ossreviewtoolkit.workbench.model.LicenseFindingWithProvenance
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

/**
 * Data class holding the state of the curation dialog for a specific license finding.
 */
data class CurationDialogData(
    val finding: LicenseFindingWithProvenance,
    val packageId: Identifier?,
    val detectedLicense: String,
    val path: String,
    val startLine: Int,
    val endLine: Int,
    val reason: LicenseFindingCurationReason = LicenseFindingCurationReason.INCORRECT,
    val concludedLicense: String = "NONE",
    val comment: String = ""
)
