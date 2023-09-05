package org.ossreviewtoolkit.workbench.model

data class FilterData<ITEM : Any>(
    val options: List<ITEM> = emptyList(),
    val selectedItem: ITEM? = null
) {
    /**
     * Return a new [FilterData] instance with the provided options and the currently [selectedItem]. If the provided
     * [options] do not contain the [selectedItem] it is set to null.
     */
    fun updateOptions(options: List<ITEM>) = FilterData(options, selectedItem.takeIf { it in options })
}
