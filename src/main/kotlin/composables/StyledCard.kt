package org.ossreviewtoolkit.workbench.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun StyledCard(titleIcon: Painter? = null, title: String, content: @Composable (ColumnScope.() -> Unit)) =
    Card(modifier = Modifier.fillMaxWidth(), elevation = 8.dp) {
        Column {
            Surface(color = MaterialTheme.colors.primaryVariant, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    titleIcon?.run { Icon(titleIcon, contentDescription = title, modifier = Modifier.size(24.dp)) }
                    Text(title, style = MaterialTheme.typography.h4)
                }
            }

            Column(modifier = Modifier.padding(15.dp)) {
                content()
            }
        }
    }
