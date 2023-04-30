package org.ossreviewtoolkit.workbench.ui.packages

data class PackagesState(
    val packages: List<PackageInfo>,
    val filter: PackagesFilter
) {
    companion object {
        val INITIAL = PackagesState(
            packages = emptyList(),
            filter = PackagesFilter()
        )
    }
}
