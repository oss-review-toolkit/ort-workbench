package org.ossreviewtoolkit.workbench.ui.dependencies

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import org.ossreviewtoolkit.model.PackageLinkage
import org.ossreviewtoolkit.model.PackageReference
import org.ossreviewtoolkit.model.Project
import org.ossreviewtoolkit.model.Scope
import org.ossreviewtoolkit.workbench.composables.tree.TreeNode
import org.ossreviewtoolkit.workbench.lifecycle.ViewModel
import org.ossreviewtoolkit.workbench.model.OrtApi
import org.ossreviewtoolkit.workbench.model.OrtModel

class DependenciesViewModel(private val ortModel: OrtModel) : ViewModel() {
    private val defaultScope = CoroutineScope(Dispatchers.Default)

    private val _state = MutableStateFlow<DependenciesState>(DependenciesState.Loading)
    val state: StateFlow<DependenciesState> = _state

    init {
        defaultScope.launch {
            ortModel.api.collect { api ->
                val dependencyNodes = createDependencyNodes(api)

                // Switch back to the UI scope because DependenciesState contains mutable states which must be created
                // in the UI scope.
                withContext(scope.coroutineContext) {
                    _state.value = DependenciesState.Success(dependencyNodes)
                }
            }
        }
    }

    private fun createDependencyNodes(api: OrtApi): List<TreeNode<DependencyTreeItem>> {
        val resolvedLicenses = api.getProjectAndPackageIdentifiers().associateWith {
            api.getResolvedLicense(it).filterExcluded()
        }

        fun PackageReference.toTreeNode(): TreeNode<DependencyTreeItem> {
            val children = dependencies.map { it.toTreeNode() }

            return api.getProject(id)?.let { project ->
                TreeNode(
                    value = DependencyTreeProject(
                        project = project,
                        linkage = linkage,
                        issues = issues,
                        resolvedLicense = resolvedLicenses.getValue(id)
                    ),
                    key = project.id.toCoordinates(),
                    children = children
                )
            } ?: api.getCuratedPackage(id)?.let { pkg ->
                TreeNode(
                    value = DependencyTreePackage(
                        id = id,
                        pkg = pkg,
                        linkage = linkage,
                        issues = issues,
                        resolvedLicense = resolvedLicenses.getValue(id)
                    ),
                    key = id.toCoordinates(),
                    children = children
                )
            } ?: TreeNode(
                value = DependencyTreeError(
                    id = id,
                    message = "Could not find package or project for id '${id.toCoordinates()}'."
                ),
                key = id.toCoordinates()
            )
        }

        fun Scope.toTreeNode(project: Project): TreeNode<DependencyTreeItem> {
            val children = dependencies.map { it.toTreeNode() }

            return TreeNode(
                value = DependencyTreeScope(
                    project = project,
                    scope = this
                ),
                key = name,
                children = children
            )
        }

        fun Project.toTreeNode(): TreeNode<DependencyTreeItem> {
            val children = scopes.map { it.toTreeNode(this) }

            return TreeNode(
                value = DependencyTreeProject(
                    project = this,
                    linkage = PackageLinkage.PROJECT_STATIC,
                    issues = emptyList(),
                    resolvedLicense = resolvedLicenses.getValue(id)
                ),
                key = id.toCoordinates(),
                children = children
            )
        }

        return api.getProjects().map { it.toTreeNode() }
    }
}
