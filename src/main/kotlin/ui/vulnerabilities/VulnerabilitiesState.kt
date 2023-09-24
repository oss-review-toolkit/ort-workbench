package org.ossreviewtoolkit.workbench.ui.vulnerabilities

import org.ossreviewtoolkit.workbench.model.ResolvedVulnerability

sealed interface VulnerabilitiesState {
    data object Loading : VulnerabilitiesState

    data class Success(
        val vulnerabilities: List<ResolvedVulnerability>,
        val filter: VulnerabilitiesFilter
    ) : VulnerabilitiesState
}
