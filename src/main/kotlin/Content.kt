package org.ossreviewtoolkit.workbench

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

@Composable
fun Content(
    menuState: MutableState<MenuItem>
) {
    when (menuState.value) {
        MenuItem.SETTINGS -> Settings()
        MenuItem.DEPENDENCIES -> Dependencies()
        MenuItem.ISSUES -> Issues()
        MenuItem.RULE_VIOLATIONS -> RuleViolations()
        MenuItem.VULNERABILITIES -> Vulnerabilities()
    }
}
