package org.ossreviewtoolkit.workbench.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

@Composable
fun <SCREEN : Screen> Navigation(
    navigationController: NavigationController<SCREEN>,
    content: @Composable (screen: SCREEN?) -> Unit
) {
    val currentScreen = navigationController.currentScreen.collectAsState()
    content(currentScreen.value)
}
