package org.ossreviewtoolkit.workbench.util

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun FilterTextField(filterText: String, onFilterChange: (String) -> Unit) {
    val filterIcon = painterResource(MaterialIcon.FILTER.resource)

    TextField(
        value = filterText,
        onValueChange = onFilterChange,
        placeholder = { Text("Filter") },
        singleLine = true,
        leadingIcon = { Icon(filterIcon, "Filter") },
        shape = RoundedCornerShape(10.dp),
        colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        modifier = Modifier.widthIn(max = 300.dp)
    )
}

@Composable
@Preview
private fun FilterTextFieldPreview() {
    FilterTextField("Filter text", onFilterChange = {})
}
