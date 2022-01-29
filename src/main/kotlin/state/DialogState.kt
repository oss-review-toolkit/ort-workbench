package org.ossreviewtoolkit.workbench.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import kotlinx.coroutines.CompletableDeferred

class DialogState<T> {
    private var onResult: CompletableDeferred<T>? by mutableStateOf(null)

    val isAwaiting get() = onResult != null

    suspend fun awaitResult(): T {
        onResult = CompletableDeferred()
        val result = onResult!!.await()
        onResult = null
        return result
    }

    fun onResult(result: T) = onResult!!.complete(result)
}
