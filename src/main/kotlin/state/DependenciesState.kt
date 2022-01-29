package org.ossreviewtoolkit.workbench.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import org.ossreviewtoolkit.model.CuratedPackage
import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.OrtIssue
import org.ossreviewtoolkit.model.OrtResult
import org.ossreviewtoolkit.model.PackageLinkage
import org.ossreviewtoolkit.model.PackageReference
import org.ossreviewtoolkit.model.Project
import org.ossreviewtoolkit.model.Scope
import org.ossreviewtoolkit.model.licenses.ResolvedLicenseInfo
import org.ossreviewtoolkit.workbench.util.OrtResultApi

class DependenciesState {
    var initialized by mutableStateOf(false)
        private set

    var error: String? by mutableStateOf(null)
        private set

    private var resultApi = OrtResultApi(OrtResult.EMPTY)

    private val dependencyTreeItems = mutableStateListOf<DependencyTreeItem>()

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

    suspend fun initialize(resultApi: OrtResultApi) {
        this.resultApi = resultApi
        dependencyTreeItems.clear()
        dependencyTreeItems += createDependencyTreeItemsAsnyc()
        updateFilteredDependencyTreeItems()
        initialized = true
    }

    private suspend fun createDependencyTreeItemsAsnyc(): List<DependencyTreeItem> =
        withContext(Dispatchers.Default) {
            val resolvedLicenses = resultApi.result.collectProjectsAndPackages().associateWith {
                resultApi.licenseInfoResolver.resolveLicenseInfo(it).filterExcluded()
            }

            buildList {
                var index = 0
                resultApi.result.getProjects().forEach { project ->
                    add(
                        DependencyTreeProject(
                            index,
                            level = 0,
                            project = project,
                            linkage = PackageLinkage.PROJECT_STATIC,
                            issues = emptyList(),
                            resolvedLicense = resolvedLicenses.getValue(project.id)
                        )
                    )
                    index++

                    project.scopes.forEach { scope ->
                        add(DependencyTreeScope(index, level = 1, project, scope))
                        index++

                        fun addDependency(level: Int, pkgRef: PackageReference) {
                            resultApi.result.getProject(pkgRef.id)?.let { project ->
                                add(
                                    DependencyTreeProject(
                                        index = index,
                                        level = level,
                                        project = project,
                                        linkage = pkgRef.linkage,
                                        issues = pkgRef.issues,
                                        resolvedLicense = resolvedLicenses.getValue(pkgRef.id)
                                    )
                                )
                                index++
                            }

                            // TODO: Handle cases where the package is missing in the package list.
                            resultApi.result.getPackage(pkgRef.id).let { pkg ->
                                add(
                                    DependencyTreePackage(
                                        index = index,
                                        level = level,
                                        hasChildren = pkgRef.dependencies.isNotEmpty(),
                                        id = pkgRef.id,
                                        pkg = pkg,
                                        linkage = pkgRef.linkage,
                                        issues = pkgRef.issues,
                                        resolvedLicense = resolvedLicenses[pkgRef.id]
                                    )
                                )
                                index++
                            }

                            pkgRef.dependencies.forEach { addDependency(level + 1, it) }
                        }

                        scope.dependencies.forEach { pkgRef ->
                            addDependency(level = 2, pkgRef)
                        }
                    }
                }
            }
        }

    fun collapseAll() {
        dependencyTreeItems.forEach { it.expanded = false }
        updateFilteredDependencyTreeItems()
    }

    fun expandAll() {
        dependencyTreeItems.forEach { it.expanded = true }
        updateFilteredDependencyTreeItems()
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
