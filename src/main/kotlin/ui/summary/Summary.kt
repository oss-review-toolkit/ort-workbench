package org.ossreviewtoolkit.workbench.ui.summary

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import org.ossreviewtoolkit.workbench.composables.CircularProgressBox

@Composable
fun Summary(viewModel: SummaryViewModel) {
    val stateState = viewModel.state.collectAsState()

    when (val state = stateState.value) {
        is SummaryState.Loading -> CircularProgressBox()

        is SummaryState.Success -> {
            val scrollState = rememberScrollState()

            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        ResultFileInfoCard(state.resultFileInfo)
                        AnalyzerInfoCard(state.analyzerInfo)
                        AdvisorInfoCard(state.advisorInfo)
                        ScannerInfoCard(state.scannerInfo)
                        EvaluatorInfoCard(state.evaluatorInfo)
                    }
                }

                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(scrollState = scrollState)
                )
            }
        }
    }
}
