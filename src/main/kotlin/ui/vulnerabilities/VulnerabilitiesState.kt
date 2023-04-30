package org.ossreviewtoolkit.workbench.ui.vulnerabilities

import org.ossreviewtoolkit.workbench.model.ResolvedVulnerability

data class VulnerabilitiesState(
    val vulnerabilities: List<ResolvedVulnerability>,
    val filter: VulnerabilitiesFilter
) {
    companion object {
        val INITIAL = VulnerabilitiesState(
            vulnerabilities = emptyList(),
            filter = VulnerabilitiesFilter()
        )
    }
}
