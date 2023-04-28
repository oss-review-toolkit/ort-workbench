package org.ossreviewtoolkit.workbench.navigation

import org.ossreviewtoolkit.workbench.lifecycle.ViewModel

/**
 * An entry on the navigation backstack that contains the [screen] configuration and the associated [viewModel].
 */
class BackstackEntry(
    val screen: Screen<*>,
    val viewModel: ViewModel
)
