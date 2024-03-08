package org.ossreviewtoolkit.workbench.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
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
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

import org.ossreviewtoolkit.utils.ort.Environment
import org.ossreviewtoolkit.workbench.composables.Preview
import org.ossreviewtoolkit.workbench.composables.Tooltip
import org.ossreviewtoolkit.workbench.composables.conditional
import org.ossreviewtoolkit.workbench.composables.enumcase
import org.ossreviewtoolkit.workbench.model.OrtApiState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Menu(
    currentItem: MenuItem?,
    apiState: OrtApiState,
    useOnlyResolvedConfiguration: Boolean,
    onSwitchUseOnlyResolvedConfiguration: () -> Unit,
    onSelectMenuItem: (MenuItem) -> Unit
) {
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

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Use only resolved configuration",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.weight(1f)
                    )

                    TooltipArea(
                        tooltipPlacement = TooltipPlacement.ComponentRect(
                            anchor = Alignment.TopCenter,
                            alignment = Alignment.TopCenter
                        ),
                        tooltip = {
                            Tooltip(
                                """
                                    Use only the resolved configuration from the ORT result and ignore the local ORT
                                    config directory. This can be disabled to test local configuration changes, but be
                                    aware that this can lead to inconsistent results when the ORT result was created
                                    with different configuration.
                                    Please note that this setting currently only affects package configurations and
                                    resolutions.
                                """.trimIndent()
                            )
                        }
                    ) {
                        Switch(
                            checked = useOnlyResolvedConfiguration,
                            onCheckedChange = { onSwitchUseOnlyResolvedConfiguration() }
                        )
                    }
                }

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
        Menu(currentItem = MenuItem.SUMMARY, OrtApiState.READY, true, {}, {})
    }
}
