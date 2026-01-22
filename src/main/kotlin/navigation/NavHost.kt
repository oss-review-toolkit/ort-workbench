package org.ossreviewtoolkit.workbench.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun NavHost(navigationController: NavController, content: @Composable (backstackEntry: BackstackEntry) -> Unit) {
    val backstackEntry by navigationController.backstackEntry.collectAsState()

    backstackEntry?.let { content(it) }
}
