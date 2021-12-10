package org.ossreviewtoolkit.workbench

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.ossreviewtoolkit.utils.common.titlecase

enum class MenuItem(val icon: ImageVector) {
    SETTINGS(Icons.Filled.Settings),
    DEPENDENCIES(Icons.Filled.AccountTree),
    ISSUES(Icons.Filled.BugReport),
    RULE_VIOLATIONS(Icons.Filled.Gavel),
    VULNERABILITIES(Icons.Filled.LockOpen);

    val readableName: String by lazy { name.split("_").joinToString(" ") { it.titlecase() } }
}

@Composable
@Preview
fun Menu(menuState: MutableState<MenuItem>) {
    Column(
        modifier = Modifier.padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        MenuItem.values().forEach { item ->
            val isSelected = item == menuState.value

            Row(
                modifier = Modifier
                    .clickable { menuState.value = item },
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(item.icon, item.readableName)

                Text(
                    text = item.readableName,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
