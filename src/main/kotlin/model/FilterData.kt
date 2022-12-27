package org.ossreviewtoolkit.workbench.model

data class FilterData<ITEM : Any>(
    val selectedItem: ITEM?,
    val options: List<ITEM>
)
