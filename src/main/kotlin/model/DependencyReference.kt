package org.ossreviewtoolkit.workbench.model

import org.ossreviewtoolkit.model.Identifier

data class DependencyReference(
    val project: Identifier,
    val isExcluded: Boolean,
    val scopes: List<ScopeReference>
)

data class ScopeReference(
    val scope: String,
    val isExcluded: Boolean
)
