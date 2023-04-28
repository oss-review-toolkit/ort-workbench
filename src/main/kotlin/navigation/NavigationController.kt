package org.ossreviewtoolkit.workbench.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NavigationController<SCREEN : OldScreen>(initialScreen: SCREEN? = null) {
    private val stack = ArrayDeque<SCREEN>()

    private val _currentScreen = MutableStateFlow<SCREEN?>(null)
    val currentScreen: StateFlow<SCREEN?> get() = _currentScreen

    init {
        if (initialScreen != null) push(initialScreen)
    }

    fun push(screen: SCREEN) {
        stack.addLast(screen)
        _currentScreen.value = peek()
    }

    fun replace(screen: SCREEN) {
        stack.clear()
        push(screen)
    }

    fun pop(): SCREEN? = stack.removeLastOrNull().also { _currentScreen.value = peek() }

    fun peek(): SCREEN? = stack.lastOrNull()
}

@Composable
fun <SCREEN : OldScreen> rememberNavigationController(initialScreen: SCREEN? = null) =
    remember { NavigationController(initialScreen) }
