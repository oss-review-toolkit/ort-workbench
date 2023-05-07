package org.ossreviewtoolkit.workbench.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

import org.ossreviewtoolkit.workbench.composables.conditional
import org.ossreviewtoolkit.workbench.model.OrtModelInfo
import org.ossreviewtoolkit.workbench.utils.MaterialIcon

@Composable
fun TopBar(
    selectedOrtModel: OrtModelInfo?,
    ortModelInfos: List<OrtModelInfo>,
    onLoadFile: () -> Unit,
    onSelectModel: (OrtModelInfo) -> Unit,
    onCloseModel: (OrtModelInfo) -> Unit
) {
    TopAppBar(modifier = Modifier.zIndex(zIndex = 5f), backgroundColor = MaterialTheme.colors.primaryVariant) {
        Image(
            painter = painterResource("ort-white.png"),
            contentDescription = "OSS Review Toolkit",
            contentScale = ContentScale.FillHeight,
            modifier = Modifier.padding(vertical = 10.dp).width(200.dp)
        )

        Box(modifier = Modifier.weight(1f))

        OrtModelSelector(
            selectedOrtModel,
            ortModelInfos,
            onLoadFile = onLoadFile,
            onSelectModel = onSelectModel,
            onCloseModel = onCloseModel
        )
    }
}

@Composable
fun OrtModelSelector(
    selectedOrtModel: OrtModelInfo?,
    ortModelInfos: List<OrtModelInfo>,
    onLoadFile: () -> Unit,
    onSelectModel: (OrtModelInfo) -> Unit,
    onCloseModel: (OrtModelInfo) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = !expanded }) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colors.onPrimary) {
                Text(selectedOrtModel?.name ?: "")

                val resource = (if (expanded) MaterialIcon.EXPAND_LESS else MaterialIcon.EXPAND_MORE).resource
                Icon(painterResource(resource), if (expanded) "expand" else "collapse")
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.width(600.dp)) {
            DropdownMenuItem(onClick = {
                expanded = false
                onLoadFile()
            }) {
                Text("Open ORT result file")
            }

            ortModelInfos.forEach { modelInfo ->
                Divider()

                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onSelectModel(modelInfo)
                    },
                    modifier = Modifier.fillMaxWidth()
                        .conditional(modelInfo == selectedOrtModel) { background(MaterialTheme.colors.background) }
                ) {
                    val projects = modelInfo.projectsByPackageManager.entries.joinToString { "${it.value} ${it.key}" }

                    Row(modifier = Modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(modelInfo.name, fontWeight = FontWeight.Bold, overflow = TextOverflow.Ellipsis)

                            Text(modelInfo.filePath, overflow = TextOverflow.Ellipsis)

                            Text("Projects: $projects", fontStyle = FontStyle.Italic)
                        }

                        IconButton(onClick = { onCloseModel(modelInfo) }) {
                            Icon(
                                painterResource(MaterialIcon.CLOSE.resource),
                                contentDescription = "close",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
