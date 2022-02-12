package org.ossreviewtoolkit.workbench.ui

import androidx.compose.runtime.Composable

import com.halilibo.richtext.ui.material.SetupMaterialRichText

import org.ossreviewtoolkit.workbench.state.AppState

@Composable
fun Content(state: AppState) {
    SetupMaterialRichText {
        when (state.currentScreen) {
            MenuItem.SUMMARY -> Summary(state)
            MenuItem.DEPENDENCIES -> Dependencies(state)
            MenuItem.ISSUES -> Issues(state)
            MenuItem.RULE_VIOLATIONS -> Violations(state)
            MenuItem.VULNERABILITIES -> Vulnerabilities(state)
            MenuItem.SETTINGS -> Settings(state)
        }
    }
}
