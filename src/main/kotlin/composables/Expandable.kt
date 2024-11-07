package org.ossreviewtoolkit.workbench.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun Expandable(
    header: @Composable ColumnScope.(Boolean) -> Unit,
    startExpanded: Boolean = false,
    expandedContent: @Composable () -> Unit
) {
    val expanded = remember { MutableTransitionState(startExpanded) }

    Row {
        Column(modifier = Modifier.weight(1f).align(Alignment.Bottom).animateContentSize()) {
            header(expanded.currentState)
        }

        IconButton(
            onClick = { expanded.targetState = !expanded.currentState },
            modifier = Modifier.align(Alignment.Top)
        ) {
            val image = if (expanded.currentState) Icons.Default.ExpandLess else Icons.Default.ExpandMore
            Icon(image, "expand")
        }
    }

    AnimatedVisibility(
        visibleState = expanded,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        expandedContent()
    }
}
