package org.ossreviewtoolkit.workbench.navigation

import org.ossreviewtoolkit.workbench.lifecycle.ViewModel

/**
 * An interface of screens that can be navigated to.
 */
interface Screen<VM : ViewModel> {
    /**
     * A factory function to create the [ViewModel] for this [Screen].
     */
    fun createViewModel(): VM
}
