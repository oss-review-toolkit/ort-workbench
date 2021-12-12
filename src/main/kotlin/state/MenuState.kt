package org.ossreviewtoolkit.workbench.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.ossreviewtoolkit.workbench.ui.MenuItem

class MenuState {
    var screen by mutableStateOf(MenuItem.SUMMARY)
        private set

    fun switchScreen(menuItem: MenuItem) {
        screen = menuItem
    }
}
