package org.ossreviewtoolkit.workbench.lifecycle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * The [ViewModel] manages the UI state, similar to view models in Android. It is supposed to exist outside the Compose
 * scope so that it can preserve state independent of recompositions.
 */
open class ViewModel {
    /**
     * A [CoroutineScope] similar to the one used in Android view models. It uses a [SupervisorJob] to ensure that jobs
     * in this scope can fail independently of each other, combined with an
     * [immediate][MainCoroutineDispatcher.immediate] dispatcher.
     */
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /**
     * Close this [ViewModel] by cancelling its coroutine [scope]. After calling this function this [ViewModel] should
     * not be used anymore.
     */
    fun close() {
        scope.cancel()
    }
}
