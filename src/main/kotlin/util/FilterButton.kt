package org.ossreviewtoolkit.workbench.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun <T> FilterButton(
    selectedItem: T,
    items: List<T>,
    buttonWidth: Dp = 130.dp,
    dropdownWidth: Dp = 130.dp,
    onFilterChange: (T) -> Unit,
    buttonContent: @Composable RowScope.(selectedItem: T) -> Unit,
    dropdownItem: @Composable RowScope.(item: T) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = !expanded }, modifier = Modifier.width(buttonWidth)) {
            buttonContent(selectedItem)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(dropdownWidth)
        ) {
            items.forEach { item ->
                DropdownMenuItem(onClick = {
                    expanded = false
                    onFilterChange(item)
                }) {
                    dropdownItem(item)
                }
            }
        }
    }
}
