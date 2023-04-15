package org.ossreviewtoolkit.workbench.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun <SCREEN : Screen> Navigation(
    navigationController: NavigationController<SCREEN>,
    content: @Composable (screen: SCREEN?) -> Unit
) {
    val currentScreen by navigationController.currentScreen.collectAsState()
    content(currentScreen)
}
