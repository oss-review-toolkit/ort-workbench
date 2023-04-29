@file:OptIn(ExperimentalComposeUiApi::class)

package org.ossreviewtoolkit.workbench.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

import org.ossreviewtoolkit.utils.ort.Environment
import org.ossreviewtoolkit.workbench.composables.Preview
import org.ossreviewtoolkit.workbench.composables.conditional
import org.ossreviewtoolkit.workbench.composables.enumcase
import org.ossreviewtoolkit.workbench.model.OrtApiState

@Composable
fun Menu(currentItem: MenuItem?, apiState: OrtApiState, onSelectMenuItem: (MenuItem) -> Unit) {
    Surface(modifier = Modifier.fillMaxHeight().width(200.dp).zIndex(zIndex = 3f), elevation = 8.dp) {
        Column(
            modifier = Modifier.padding(vertical = 20.dp)
        ) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                MenuRow(MenuItem.SUMMARY, currentItem, apiState) { onSelectMenuItem(MenuItem.SUMMARY) }
                MenuRow(MenuItem.PACKAGES, currentItem, apiState) { onSelectMenuItem(MenuItem.PACKAGES) }
                MenuRow(MenuItem.DEPENDENCIES, currentItem, apiState) { onSelectMenuItem(MenuItem.DEPENDENCIES) }
                MenuRow(MenuItem.ISSUES, currentItem, apiState) { onSelectMenuItem(MenuItem.ISSUES) }
                MenuRow(MenuItem.RULE_VIOLATIONS, currentItem, apiState) { onSelectMenuItem(MenuItem.RULE_VIOLATIONS) }
                MenuRow(MenuItem.VULNERABILITIES, currentItem, apiState) { onSelectMenuItem(MenuItem.VULNERABILITIES) }

                Box(modifier = Modifier.weight(1f))

                Divider()

                MenuRow(MenuItem.SETTINGS, currentItem, apiState) { onSelectMenuItem(MenuItem.SETTINGS) }

                Divider()

                Text(
                    "ORT version ${Environment.ORT_VERSION}",
                    modifier = Modifier.fillMaxWidth().padding(top = 15.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}

@Composable
fun MenuRow(item: MenuItem, currentItem: MenuItem?, apiState: OrtApiState, onSelect: () -> Unit) {
    val isEnabled = item == MenuItem.SUMMARY || apiState == OrtApiState.READY

    if (isEnabled) {
        val isCurrent = item == currentItem

        Row(
            modifier = Modifier.clickable { onSelect() }
                .conditional(isCurrent) { background(MaterialTheme.colors.background) }
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(item.icon.resource), item.name)

            Text(
                text = item.name.enumcase(),
                style = MaterialTheme.typography.h6,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
@Preview
private fun MenuPreview() {
    Preview {
        Menu(currentItem = MenuItem.SUMMARY, OrtApiState.READY) {}
    }
}
