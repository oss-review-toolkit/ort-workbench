package org.ossreviewtoolkit.workbench

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
@Preview
fun App() {
    val menuState = rememberSaveable { mutableStateOf(MenuItem.SETTINGS) }

    MaterialTheme {
        Row(
            modifier = Modifier.fillMaxWidth().fillMaxHeight()
        ) {
            Surface(
                modifier = Modifier.fillMaxHeight().width(200.dp),
                elevation = 8.dp
            ) {
                Menu(menuState)
            }

            Column(
                modifier = Modifier.padding(5.dp)
            ) {
                Content(menuState)
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication
    ) {
        App()
    }
}
