@file:OptIn(ExperimentalFoundationApi::class)

package org.ossreviewtoolkit.workbench.util

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

@Composable
fun WebLink(text: String, url: String) {
    TooltipArea(tooltip = { Tooltip(url) }) {
        Row(
            modifier = Modifier.clickable { openUrlInBrowser(url) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(MaterialIcon.LINK.resource),
                contentDescription = "link",
                tint = MaterialTheme.colors.primary
            )
            Text(
                text,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.primary
            )
        }
    }
}
