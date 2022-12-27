package org.ossreviewtoolkit.workbench.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import org.ossreviewtoolkit.workbench.model.FilterData

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

@Composable
fun <T> FilterButton(
    data: FilterData<T>,
    label: String,
    onFilterChange: (T) -> Unit,
    convert: (T) -> String = { it.toString() }
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = !expanded }) {
            if (data.selectedItem == null) {
                Text(label, overflow = TextOverflow.Ellipsis)
            } else {
                Text(convert(data.selectedItem), overflow = TextOverflow.Ellipsis)
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            data.options.forEach { item ->
                DropdownMenuItem(onClick = {
                    expanded = false
                    onFilterChange(item)
                }) {
                    if (item == null) {
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Text("No filter", fontStyle = FontStyle.Italic, overflow = TextOverflow.Ellipsis)
                        }
                    } else {
                        Text(convert(item), overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
