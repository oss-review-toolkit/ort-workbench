package org.ossreviewtoolkit.workbench.ui.packages

sealed interface PackagesState {
    data class Loading(
        val processedPackages: Int,
        val totalPackages: Int
    ) : PackagesState

    data class Success(
        val packages: List<PackageInfo>,
        val filter: PackagesFilter
    ) : PackagesState
}
