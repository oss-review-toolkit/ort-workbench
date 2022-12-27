package org.ossreviewtoolkit.workbench.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
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

import org.ossreviewtoolkit.workbench.model.FilterData

@Composable
fun <T : Any> FilterButton(
    data: FilterData<T>,
    label: String,
    onFilterChange: (T?) -> Unit,
    convert: (T) -> String = { it.toString() }
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
            if (data.selectedItem == null) {
                Text(label, overflow = TextOverflow.Ellipsis)
            } else {
                Text("$label: ${convert(data.selectedItem)}", overflow = TextOverflow.Ellipsis)
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(onClick = {
                expanded = false
                onFilterChange(null)
            }) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text("No filter", fontStyle = FontStyle.Italic, overflow = TextOverflow.Ellipsis)
                }
            }

            data.options.forEach { item ->
                DropdownMenuItem(onClick = {
                    expanded = false
                    onFilterChange(item)
                }) {
                    Text(convert(item), overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
