package org.ossreviewtoolkit.workbench.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun Tooltip(text: String) {
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }

    AnimatedVisibility(
        visibleState = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(modifier = Modifier.clip(RoundedCornerShape(size = 8.dp)), contentAlignment = Alignment.Center) {
            Row(modifier = Modifier.background(MaterialTheme.colors.primaryVariant).padding(8.dp)) {
                Text(text, style = MaterialTheme.typography.caption, color = MaterialTheme.colors.onPrimary)
            }
        }
    }
}

@Composable
@Preview
private fun TooltipPreview() {
    Preview {
        Tooltip("Some tooltip")
    }
}
