@file:OptIn(ExperimentalComposeUiApi::class)

package org.ossreviewtoolkit.workbench.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import org.ossreviewtoolkit.utils.common.titlecase
import org.ossreviewtoolkit.utils.core.Environment
import org.ossreviewtoolkit.workbench.state.MenuState
import org.ossreviewtoolkit.workbench.state.ResultStatus
import org.ossreviewtoolkit.workbench.util.MaterialIcon
import org.ossreviewtoolkit.workbench.util.Preview

enum class MenuItem(val icon: MaterialIcon) {
    SUMMARY(MaterialIcon.ASSESSMENT),
    DEPENDENCIES(MaterialIcon.ACCOUNT_TREE),
    ISSUES(MaterialIcon.BUG_REPORT),
    RULE_VIOLATIONS(MaterialIcon.GAVEL),
    VULNERABILITIES(MaterialIcon.LOCK_OPEN);

    val readableName: String by lazy { name.split("_").joinToString(" ") { it.titlecase() } }
}

@Composable
fun Menu(state: MenuState, resultStatus: ResultStatus) {
    Column(
        modifier = Modifier.padding(vertical = 20.dp)
    ) {
        if (resultStatus == ResultStatus.FINISHED) {
            Box(modifier = Modifier.width(180.dp).padding(start = 20.dp, bottom = 25.dp)) {
                Image(
                    painter = painterResource("ort-white.png"),
                    contentDescription = "OSS Review Toolkit",
                    contentScale = ContentScale.FillWidth
                )
            }
        }

        MenuItem.values().forEach { item ->
            MenuRow(state, resultStatus, item)
        }

        Box(modifier = Modifier.weight(1f))

        Text(
            "ORT version ${Environment.ORT_VERSION}",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.caption
        )
    }
}

@Composable
fun MenuRow(state: MenuState, resultStatus: ResultStatus, item: MenuItem) {
    val isSelected = item == state.screen
    val isEnabled = item == MenuItem.SUMMARY || resultStatus == ResultStatus.FINISHED

    if (isEnabled) {
        Row(
            modifier = Modifier.clickable { state.switchScreen(item) }
                .background(if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.primaryVariant)
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(item.icon.resource), item.readableName)

            Text(
                text = item.readableName,
                style = MaterialTheme.typography.subtitle2,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
@Preview
private fun MenuPreview() {
    Preview {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.primaryVariant) {
            Menu(MenuState(), ResultStatus.FINISHED)
        }
    }
}
