package org.ossreviewtoolkit.workbench.ui

import androidx.compose.runtime.Composable
import org.ossreviewtoolkit.workbench.state.AppState

@Composable
fun Content(state: AppState) {
    when (state.menu.screen) {
        MenuItem.SUMMARY -> Summary(state)
        MenuItem.DEPENDENCIES -> Dependencies(state)
        MenuItem.ISSUES -> Issues(state)
        MenuItem.RULE_VIOLATIONS -> Violations(state)
        MenuItem.VULNERABILITIES -> Vulnerabilities(state)
    }
}
