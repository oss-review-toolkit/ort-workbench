package org.ossreviewtoolkit.workbench.model

data class FilterData<ITEM>(
    val selectedItem: ITEM,
    val options: List<ITEM>
)
