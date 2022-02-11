package org.ossreviewtoolkit.workbench.ui.dependencies

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import org.ossreviewtoolkit.model.CuratedPackage
import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.OrtIssue
import org.ossreviewtoolkit.model.PackageLinkage
import org.ossreviewtoolkit.model.Project
import org.ossreviewtoolkit.model.Scope
import org.ossreviewtoolkit.model.licenses.ResolvedLicenseInfo

class DependenciesState(private val dependencyTreeItems: List<DependencyTreeItem>) {
    var initialized by mutableStateOf(false)
        private set

    var error: String? by mutableStateOf(null)
        private set

    private val _filteredDependencyTreeItems = mutableStateListOf<DependencyTreeItem>()
    val filteredDependencyTreeItems: List<DependencyTreeItem> get() = _filteredDependencyTreeItems

    var search by mutableStateOf("")
        private set

    var searchCurrentHit by mutableStateOf(-1)
        private set

    private val _searchHits = mutableStateListOf<Int>()
    val searchHits: List<Int> get() = _searchHits

    var isItemAutoSelected by mutableStateOf(false)
        private set

    var selectedItem: DependencyTreeItem? by mutableStateOf(null)
        private set

    suspend fun initialize() {
        withContext(Dispatchers.Default) {
            updateFilteredDependencyTreeItems()
            initialized = true
        }
    }

    fun selectItem(item: DependencyTreeItem, isAutoSelected: Boolean) {
        selectedItem = item
        isItemAutoSelected = isAutoSelected
    }

    private fun selectItem(itemIndex: Int, isAutoSelected: Boolean) {
        dependencyTreeItems.getOrNull(itemIndex)?.let { selectItem(it, isAutoSelected = isAutoSelected) }
    }

    fun selectNextSearchHit() {
        if (searchHits.isEmpty()) searchCurrentHit = -1
        else searchCurrentHit++

        if (searchCurrentHit >= searchHits.size) searchCurrentHit = 0

        if (searchCurrentHit > -1) {
            expandTree(searchHits[searchCurrentHit])
            selectItem(searchHits[searchCurrentHit], isAutoSelected = true)
        }
    }

    fun selectPreviousSearchHit() {
        if (searchHits.isEmpty()) searchCurrentHit = -1
        else searchCurrentHit--

        if (searchCurrentHit < 0) searchCurrentHit = searchHits.size - 1

        if (searchCurrentHit > -1) {
            expandTree(searchHits[searchCurrentHit])
            selectItem(searchHits[searchCurrentHit], isAutoSelected = true)
        }
    }

    private fun expandTree(itemIndex: Int) {
        var currentItem = dependencyTreeItems.getOrNull(itemIndex)

        do {
            currentItem = currentItem?.let { getItemParent(it) }
            currentItem?.expanded = true
        } while (currentItem != null && currentItem.level >= 0)

        updateFilteredDependencyTreeItems()
    }

    private fun getItemParent(item: DependencyTreeItem): DependencyTreeItem? {
        var index = item.index - 1
        while (index >= 0) {
            if (dependencyTreeItems[index].level < item.level) return dependencyTreeItems[index]
            index--
        }

        return null
    }

    fun toggleExpanded(item: DependencyTreeItem) {
        item.expanded = !item.expanded
        updateFilteredDependencyTreeItems()
    }

    fun updateSearch(search: String) {
        this.search = search
        searchCurrentHit = 0
        _searchHits.clear()

        if (search.isNotBlank()) {
            val trimmedSearch = search.trim()

            _searchHits += dependencyTreeItems.mapIndexedNotNull { index, item ->
                if (item.name.contains(trimmedSearch)) index else null
            }
        }

        searchCurrentHit = if (_searchHits.isEmpty()) -1 else 0

        if (searchCurrentHit >= 0) {
            val item = dependencyTreeItems[searchHits[searchCurrentHit]]
            expandTree(item.index)
            selectItem(item, isAutoSelected = true)
        }
    }

    private fun updateFilteredDependencyTreeItems() {
        _filteredDependencyTreeItems.clear()

        var expandedLevel = 0

        dependencyTreeItems.forEach { item ->
            if (item.level < expandedLevel) expandedLevel = item.level
            if (item.level <= expandedLevel) {
                _filteredDependencyTreeItems += item
                if (item.expanded) expandedLevel = item.level + 1
            }
        }
    }
}

sealed class DependencyTreeItem(val index: Int, val level: Int, val hasChildren: Boolean) {
    abstract val name: String

    var expanded by mutableStateOf(false)
}

class DependencyTreeProject(
    index: Int,
    level: Int,
    val project: Project,
    val linkage: PackageLinkage,
    val issues: List<OrtIssue>,
    val resolvedLicense: ResolvedLicenseInfo
) : DependencyTreeItem(index, level, hasChildren = project.scopes.isNotEmpty()) {
    override val name = project.id.toCoordinates()
}

class DependencyTreeScope(index: Int, level: Int, val project: Project, val scope: Scope) :
    DependencyTreeItem(index, level, hasChildren = scope.dependencies.isNotEmpty()) {
    override val name = scope.name
}

class DependencyTreePackage(
    index: Int,
    level: Int,
    hasChildren: Boolean,
    val id: Identifier,
    val pkg: CuratedPackage?,
    val linkage: PackageLinkage,
    val issues: List<OrtIssue>,
    val resolvedLicense: ResolvedLicenseInfo?
) : DependencyTreeItem(index, level, hasChildren) {
    override val name = id.toCoordinates()
}
