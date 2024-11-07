package org.ossreviewtoolkit.workbench.composables

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun FilterTextField(
    filterText: String,
    label: String = "Filter",
    image: ImageVector = Icons.Default.Filter,
    modifier: Modifier = Modifier,
    onFilterChange: (String) -> Unit
) {
    var localFilterText by remember(filterText) { mutableStateOf(filterText) }

    TextField(
        value = localFilterText,
        onValueChange = {
            localFilterText = it
            onFilterChange(it)
        },
        placeholder = { Text(label) },
        singleLine = true,
        leadingIcon = { Icon(image, label) },
        shape = RoundedCornerShape(10.dp),
        colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            textColor = MaterialTheme.colors.onPrimary.copy(ContentAlpha.high),
            leadingIconColor = MaterialTheme.colors.onPrimary.copy(ContentAlpha.medium),
            placeholderColor = MaterialTheme.colors.onPrimary.copy(ContentAlpha.medium)
        ),
        modifier = modifier.widthIn(max = 300.dp)
    )
}

@Composable
@Preview
private fun FilterTextFieldPreview() {
    FilterTextField("Filter text", onFilterChange = {})
}
