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

import org.ossreviewtoolkit.utils.common.titlecase
import org.ossreviewtoolkit.utils.ort.Environment
import org.ossreviewtoolkit.workbench.composables.Preview
import org.ossreviewtoolkit.workbench.composables.conditional
import org.ossreviewtoolkit.workbench.model.OrtApiState
import org.ossreviewtoolkit.workbench.utils.MaterialIcon

enum class MenuItem(val icon: MaterialIcon) {
    SUMMARY(MaterialIcon.ASSESSMENT),
    PACKAGES(MaterialIcon.INVENTORY),
    DEPENDENCIES(MaterialIcon.ACCOUNT_TREE),
    ISSUES(MaterialIcon.BUG_REPORT),
    RULE_VIOLATIONS(MaterialIcon.GAVEL),
    VULNERABILITIES(MaterialIcon.LOCK_OPEN),
    SETTINGS(MaterialIcon.SETTINGS);

    val readableName: String by lazy { name.split("_").joinToString(" ") { it.titlecase() } }
}

@Composable
fun Menu(currentScreen: MenuItem, apiState: OrtApiState, onSwitchScreen: (MenuItem) -> Unit) {
    Surface(modifier = Modifier.fillMaxHeight().width(200.dp).zIndex(zIndex = 3f), elevation = 8.dp) {
        Column(
            modifier = Modifier.padding(vertical = 20.dp)
        ) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                MenuItem.values().filter { it != MenuItem.SETTINGS }.forEach { item ->
                    MenuRow(item, isCurrent = item == currentScreen, apiState, onSwitchScreen)
                }

                Box(modifier = Modifier.weight(1f))

                Divider()

                MenuRow(MenuItem.SETTINGS, isCurrent = MenuItem.SETTINGS == currentScreen, apiState, onSwitchScreen)

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
fun MenuRow(item: MenuItem, isCurrent: Boolean, apiState: OrtApiState, onSwitchScreen: (MenuItem) -> Unit) {
    val isEnabled = item == MenuItem.SUMMARY || apiState == OrtApiState.READY

    if (isEnabled) {
        Row(
            modifier = Modifier.clickable { onSwitchScreen(item) }
                .conditional(isCurrent) { background(MaterialTheme.colors.background) }
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(item.icon.resource), item.readableName)

            Text(
                text = item.readableName,
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
        Menu(currentScreen = MenuItem.SUMMARY, OrtApiState.READY) {}
    }
}
