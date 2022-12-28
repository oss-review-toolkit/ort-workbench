package org.ossreviewtoolkit.workbench.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun ListScreenContent(
    filterText: String,
    onUpdateFilterText: (text: String) -> Unit,
    list: @Composable BoxScope.() -> Unit,
    filterPanel: @Composable RowScope.(showFilterPanel: Boolean) -> Unit
) {
    var showFilterPanel by remember { mutableStateOf(false) }

    Column {
        ListScreenAppBar(
            filterText = filterText,
            onUpdateFilterText = onUpdateFilterText,
            onToggleFilter = { showFilterPanel = !showFilterPanel }
        )

        Row {
            Box(modifier = Modifier.weight(1f)) {
                list()
            }

            filterPanel(showFilterPanel)
        }
    }
}
