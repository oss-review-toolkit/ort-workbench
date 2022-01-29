package org.ossreviewtoolkit.workbench

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.ossreviewtoolkit.utils.common.titlecase
import org.ossreviewtoolkit.workbench.state.AppState
import org.ossreviewtoolkit.workbench.state.ResultStatus
import org.ossreviewtoolkit.workbench.theme.OrtWorkbenchTheme
import org.ossreviewtoolkit.workbench.ui.Content
import org.ossreviewtoolkit.workbench.ui.Menu
import org.ossreviewtoolkit.workbench.util.FileDialog

@Composable
fun App(state: AppState) {
    OrtWorkbenchTheme {
        if (state.result.status == ResultStatus.FINISHED) {
            Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                Surface(
                    modifier = Modifier.fillMaxHeight().width(200.dp),
                    elevation = 8.dp,
                    color = MaterialTheme.colors.primaryVariant
                ) {
                    Menu(state.menu, state.result.status)
                }

                Column(
                    modifier = Modifier.padding(5.dp).fillMaxWidth()
                ) {
                    Content(state)
                }
            }
        } else {
            LoadResult(state)
        }

        if (state.openResultDialog.isAwaiting) {
            FileDialog(
                title = "Load ORT result",
                isLoad = true,
                onResult = { state.openResultDialog.onResult(it) }
            )
        }
    }
}

@Composable
fun LoadResult(state: AppState) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(25.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource("ort-black.png"), contentDescription = "OSS Review Toolkit")

        Button(onClick = { if (!state.openResultDialog.isAwaiting) scope.launch { state.openOrtResult() } }) {
            Text("Load ORT Result")
        }

        if (state.result.status in listOf(ResultStatus.LOADING, ResultStatus.PROCESSING)) {
            CircularProgressIndicator()
            Text("${state.result.status.name.titlecase()}...")
        }

        state.result.error?.let { error ->
            Card(backgroundColor = MaterialTheme.colors.error) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }
}
