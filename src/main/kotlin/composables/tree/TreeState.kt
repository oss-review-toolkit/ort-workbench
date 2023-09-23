package org.ossreviewtoolkit.workbench.composables.tree

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class TreeState<VALUE>(roots: List<TreeNode<VALUE>>, startExpanded: Boolean = false) {
    val items = buildItems(roots, startExpanded)

    var visibleItems by mutableStateOf(emptyList<TreeItem<VALUE>>())
        private set

    var selectedItem: TreeItem<VALUE>? by mutableStateOf(null)
        private set

    var isItemAutoSelected by mutableStateOf(false)
        private set

    init {
        updateVisibleItems()
    }

    private fun updateVisibleItems() {
        var expandedLevel = 0

        visibleItems = items.filter { item ->
            if (item.level < expandedLevel) expandedLevel = item.level
            if (item.level <= expandedLevel) {
                if (item.expanded) expandedLevel = item.level + 1
                true
            } else {
                false
            }
        }
    }

    fun selectItem(itemIndex: Int, isAutoSelected: Boolean) {
        items.getOrNull(itemIndex)?.let { selectItem(it, isAutoSelected = isAutoSelected) }
    }

    fun selectItem(item: TreeItem<VALUE>, isAutoSelected: Boolean) {
        if (item == selectedItem && !isAutoSelected) {
            selectedItem = null
            isItemAutoSelected = false
        } else {
            selectedItem = item
            isItemAutoSelected = isAutoSelected
        }
    }

    fun selectNextItem() {
        if (selectedItem != null) {
            val newIndex = visibleItems.indexOf(selectedItem) + 1

            selectedItem = if (newIndex < visibleItems.size) {
                visibleItems[newIndex]
            } else {
                visibleItems.first()
            }
        } else if (visibleItems.isNotEmpty()) {
            selectedItem = visibleItems[0]
        }
    }

    fun selectPreviousItem() {
        if (selectedItem != null) {
            val newIndex = visibleItems.indexOf(selectedItem) - 1

            selectedItem = if (newIndex < 0) {
                visibleItems.last()
            } else {
                visibleItems[newIndex]
            }
        } else if (visibleItems.isNotEmpty()) {
            selectedItem = visibleItems.last()
        }
    }

    fun toggleExpanded(item: TreeItem<VALUE>) {
        item.expanded = !item.expanded
        updateVisibleItems()
    }

    fun expandSelectedItem() {
        selectedItem?.let {
            if (!it.expanded) toggleExpanded(it)
        }
    }

    fun collapseSelectedItem() {
        selectedItem?.let {
            if (it.expanded) toggleExpanded(it)
        }
    }

    private fun getItemParent(item: TreeItem<VALUE>): TreeItem<VALUE>? {
        var index = item.index - 1
        while (index >= 0) {
            if (items[index].level < item.level) return items[index]
            index--
        }

        return null
    }

    fun expandItem(itemIndex: Int) {
        var currentItem = items.getOrNull(itemIndex)

        do {
            currentItem = currentItem?.let { getItemParent(it) }
            currentItem?.expanded = true
        } while (currentItem != null && currentItem.level >= 0)

        updateVisibleItems()
    }
}

private fun <VALUE> buildItems(roots: List<TreeNode<VALUE>>, startExpanded: Boolean): List<TreeItem<VALUE>> {
    var index = 0
    return buildList {
        fun addItem(level: Int, node: TreeNode<VALUE>, parentKeys: List<String> = emptyList()) {
            val newParentKeys = parentKeys + node.key
            val key = newParentKeys.joinToString(separator = "|")

            add(
                TreeItem(
                    index = index,
                    level = level,
                    node = node,
                    key = key,
                    expanded = startExpanded
                )
            )
            index++

            node.children.forEach { addItem(level = level + 1, it, newParentKeys) }
        }

        roots.forEach { addItem(level = 0, node = it) }
    }
}

class TreeNode<VALUE>(
    val value: VALUE,
    val key: String,
    val children: List<TreeNode<VALUE>> = emptyList()
)

class TreeItem<VALUE>(
    val index: Int,
    val level: Int,
    val node: TreeNode<VALUE>,
    val key: String,
    expanded: Boolean
) {
    var expanded by mutableStateOf(expanded)
}
