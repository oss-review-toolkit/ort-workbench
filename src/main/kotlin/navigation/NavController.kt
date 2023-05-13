package org.ossreviewtoolkit.workbench.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import org.ossreviewtoolkit.workbench.lifecycle.ViewModel

/**
 * The [NavController] manages the navigation backstack.
 */
class NavController(
    /**
     * The initial [Screen] to navigate to.
     */
    vararg initialScreens: Screen<*>
) {
    private val backstack = ArrayDeque<BackstackEntry>()

    private val _backstackEntry = MutableStateFlow<BackstackEntry?>(null)

    /**
     * A [StateFlow] that gets updated with the [BackstackEntry] currently at the top of the backstack.
     */
    val backstackEntry: StateFlow<BackstackEntry?> = _backstackEntry

    init {
        initialScreens.forEach {
            navigate(it, launchSingleTop = false)
        }
    }

    /**
     * Navigate to the provided [screen]. If [launchSingleTop] is true and there is already an equal screen in the
     * backstack, this screen will be moved to the top of the backstack instead of creating a new one.
     */
    fun navigate(screen: Screen<*>, launchSingleTop: Boolean = true) {
        val reuseEntry = if (launchSingleTop) backstack.find { it.screen == screen } else null

        if (reuseEntry != null) {
            backstack.remove(reuseEntry)
            backstack.addLast(reuseEntry)
        } else {
            val viewModel = screen.createViewModel()
            val newEntry = BackstackEntry(screen, viewModel)
            backstack.addLast(newEntry)
        }

        _backstackEntry.value = backstack.last()
    }

    /**
     * Remove the topmost item from the backstack.
     */
    fun back() {
        backstack.removeLastOrNull()?.let { entry ->
            entry.viewModel.close()
            _backstackEntry.value = backstack.lastOrNull()
        }
    }

    /**
     * Call this function when removing this [NavController]. This will [close][ViewModel.close] all [ViewModel]s in the
     * backstack to ensure that all resources are released.
     */
    fun close() {
        backstack.forEach { it.viewModel.close() }
    }
}
