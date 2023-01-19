package org.ossreviewtoolkit.workbench.ui.dependencies

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import org.ossreviewtoolkit.model.CuratedPackage
import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.OrtIssue
import org.ossreviewtoolkit.model.PackageLinkage
import org.ossreviewtoolkit.model.Project
import org.ossreviewtoolkit.model.Scope
import org.ossreviewtoolkit.model.licenses.ResolvedLicenseInfo
import org.ossreviewtoolkit.workbench.composables.tree.TreeNode
import org.ossreviewtoolkit.workbench.composables.tree.TreeState

class DependenciesState(rootNodes: List<TreeNode<DependencyTreeItem>>) {
    var error: String? by mutableStateOf(null)
        private set

    var search by mutableStateOf("")
        private set

    var searchCurrentHit by mutableStateOf(-1)
        private set

    private val _searchHits = mutableStateListOf<Int>()
    val searchHits: List<Int> get() = _searchHits

    val treeState: TreeState<DependencyTreeItem> = TreeState(rootNodes)

    fun selectNextSearchHit() {
        if (searchHits.isEmpty()) searchCurrentHit = -1 else searchCurrentHit++

        if (searchCurrentHit >= searchHits.size) searchCurrentHit = 0

        if (searchCurrentHit > -1) {
            treeState.expandItem(searchHits[searchCurrentHit])
            treeState.selectItem(searchHits[searchCurrentHit], isAutoSelected = true)
        }
    }

    fun selectPreviousSearchHit() {
        if (searchHits.isEmpty()) searchCurrentHit = -1 else searchCurrentHit--

        if (searchCurrentHit < 0) searchCurrentHit = searchHits.size - 1

        if (searchCurrentHit > -1) {
            treeState.expandItem(searchCurrentHit)
            treeState.selectItem(searchHits[searchCurrentHit], isAutoSelected = true)
        }
    }

    fun updateSearch(search: String) {
        this.search = search
        searchCurrentHit = 0
        _searchHits.clear()

        if (search.isNotBlank()) {
            val trimmedSearch = search.trim()

            _searchHits += treeState.items.mapIndexedNotNull { index, item ->
                if (item.node.value.name.contains(trimmedSearch)) index else null
            }
        }

        searchCurrentHit = if (_searchHits.isEmpty()) -1 else 0

        if (searchCurrentHit >= 0) {
            val item = treeState.items[searchHits[searchCurrentHit]]
            treeState.expandItem(item.index)
            treeState.selectItem(item, isAutoSelected = true)
        }
    }
}

sealed class DependencyTreeItem {
    abstract val name: String
}

class DependencyTreeProject(
    val project: Project,
    val linkage: PackageLinkage,
    val issues: List<OrtIssue>,
    val resolvedLicense: ResolvedLicenseInfo
) : DependencyTreeItem() {
    override val name = project.id.toCoordinates()
}

class DependencyTreeScope(val project: Project, val scope: Scope) : DependencyTreeItem() {
    override val name = scope.name
}

class DependencyTreePackage(
    val id: Identifier,
    val pkg: CuratedPackage?,
    val linkage: PackageLinkage,
    val issues: List<OrtIssue>,
    val resolvedLicense: ResolvedLicenseInfo?
) : DependencyTreeItem() {
    override val name = id.toCoordinates()
}

class DependencyTreeError(
    val id: Identifier,
    val message: String
) : DependencyTreeItem() {
    override val name = id.toCoordinates()
}
