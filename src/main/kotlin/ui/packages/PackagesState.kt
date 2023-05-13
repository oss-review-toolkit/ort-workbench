package org.ossreviewtoolkit.workbench.ui.packages

sealed interface PackagesState {
    object Loading : PackagesState

    data class Success(
        val packages: List<PackageInfo>,
        val filter: PackagesFilter
    ) : PackagesState
}
