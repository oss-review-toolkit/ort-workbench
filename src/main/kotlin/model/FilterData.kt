package org.ossreviewtoolkit.workbench.model

data class FilterData<ITEM : Any>(
    val options: List<ITEM> = emptyList(),
    val selectedItem: ITEM? = null
)
