@file:OptIn(ExperimentalFoundationApi::class)

package org.ossreviewtoolkit.workbench.util

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun WebLink(text: String, url: String) {
    TooltipArea(tooltip = { Tooltip(url) }) {
        Row(
            modifier = Modifier.clickable { openUrlInBrowser(url) },
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(MaterialIcon.OPEN_IN_NEW.resource),
                contentDescription = "link",
                tint = MaterialTheme.colors.primary
            )

            Text(text, color = MaterialTheme.colors.primary)
        }
    }
}

@Composable
@Preview
private fun WebLinkPreview() {
    Preview {
        WebLink("Web Link", "")
    }
}
