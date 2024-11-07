package org.ossreviewtoolkit.workbench.ui.summary

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

import org.jetbrains.compose.resources.painterResource

import org.ossreviewtoolkit.workbench.composables.Preview
import org.ossreviewtoolkit.workbench.composables.StyledCard
import org.ossreviewtoolkit.workbench.utils.OrtIcon

@Composable
fun EmptyToolInfoCard(icon: OrtIcon, toolName: String) {
    StyledCard(
        titleIcon = painterResource(icon.resource),
        title = toolName
    ) {
        Text("The $toolName was not executed.")
    }
}

@Composable
@Preview
private fun EmptyToolInfoCardPreview() {
    Preview {
        EmptyToolInfoCard(icon = OrtIcon.ANALYZER, toolName = "Analyzer")
    }
}
