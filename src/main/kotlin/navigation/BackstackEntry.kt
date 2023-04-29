package org.ossreviewtoolkit.workbench.navigation

import org.ossreviewtoolkit.workbench.lifecycle.ViewModel

/**
 * An entry on the navigation backstack that contains the [screen] configuration and the associated [viewModel].
 */
class BackstackEntry(
    val screen: Screen<*>,
    val viewModel: ViewModel
)

/**
 * Get the [ViewModel] of type [VM].
 */
inline fun <reified VM : ViewModel> BackstackEntry.viewModel(): VM = viewModel as VM
