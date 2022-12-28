package org.ossreviewtoolkit.workbench.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

import org.ossreviewtoolkit.workbench.utils.MaterialIcon

@Composable
fun ListScreenAppBar(
    filterText: String,
    onUpdateFilterText: (text: String) -> Unit,
    onToggleFilter: () -> Unit
) {
    ScreenAppBar(
        title = {},
        actions = {
            Row(modifier = Modifier.padding(vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterTextField(filterText, onFilterChange = onUpdateFilterText)

                IconButton(onClick = onToggleFilter) {
                    Icon(
                        painterResource(MaterialIcon.FILTER_LIST.resource),
                        contentDescription = "Filter",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    )
}
