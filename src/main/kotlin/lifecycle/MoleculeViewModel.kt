package org.ossreviewtoolkit.workbench.lifecycle

import androidx.compose.runtime.Composable

import app.cash.molecule.RecompositionClock
import app.cash.molecule.launchMolecule

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow

import org.jetbrains.skiko.MainUIDispatcher

/**
 * A [ViewModel] that manages a Molecule presenter, based on the official
 * [sample](https://github.com/cashapp/molecule/blob/trunk/sample-viewmodel).
 */
abstract class MoleculeViewModel<EVENT, MODEL> : ViewModel() {
    /**
     * The [CoroutineScope] used to [launch][launchMolecule] molecule.
     */
    private val moleculeScope = CoroutineScope(scope.coroutineContext + MainUIDispatcher)

    /**
     * A flow of UI events to handle. The capacity is large enough to handle simultaneous UI events, but small enough to
     * surface issues if they get backed up for some reason.
     */
    private val events = MutableSharedFlow<EVENT>(extraBufferCapacity = 20)

    /**
     * A [StateFlow] that emits new [MODEL]s on updates.
     */
    val model: StateFlow<MODEL> by lazy(LazyThreadSafetyMode.NONE) {
        moleculeScope.launchMolecule(clock = RecompositionClock.Immediate) {
            composeModel(events)
        }
    }

    /**
     * Handle the provided [event]. This might result in a new [model] being emitted. Events will be emitted to the
     * Molecule presenter function in [composeModel].
     */
    fun take(event: EVENT) {
        if (!events.tryEmit(event)) {
            error("Event buffer overflow.")
        }
    }

    /**
     * Implementations of this function are supposed to call the Molecule presenter function. This function gets
     * [launched][launchMolecule] in Molecule and recompositions will cause a new [model] to be emitted.
     */
    @Composable
    protected abstract fun composeModel(events: Flow<EVENT>): MODEL
}
