package org.ossreviewtoolkit.workbench.ui.dependencies

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import org.ossreviewtoolkit.model.PackageLinkage
import org.ossreviewtoolkit.model.PackageReference
import org.ossreviewtoolkit.workbench.model.OrtApi
import org.ossreviewtoolkit.workbench.model.OrtModel

class DependenciesViewModel(private val ortModel: OrtModel = OrtModel.INSTANCE) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _state = MutableStateFlow(DependenciesState(emptyList<DependencyTreeItem>()))
    val state: StateFlow<DependenciesState> get() = _state

    init {
        scope.launch {
            ortModel.api.collect { api ->
                _state.value = DependenciesState(createDependencyTreeItems(api))
            }
        }
    }

    private fun createDependencyTreeItems(api: OrtApi): List<DependencyTreeItem> {
        val resolvedLicenses = api.result.collectProjectsAndPackages().associateWith {
            api.licenseInfoResolver.resolveLicenseInfo(it).filterExcluded()
        }

        return buildList {
            var index = 0
            api.result.getProjects().forEach { project ->
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
                        api.result.getProject(pkgRef.id)?.let { project ->
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
                        api.result.getPackage(pkgRef.id).let { pkg ->
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
}
