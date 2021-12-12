package org.ossreviewtoolkit.workbench.util

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

@Composable
fun Expandable(header: @Composable ColumnScope.(Boolean) -> Unit, expandedContent: @Composable () -> Unit) {
    val expanded = remember { MutableTransitionState(false) }

    Row {
        Column(modifier = Modifier.weight(1f).align(Alignment.Bottom).animateContentSize()) {
            header(expanded.currentState)
        }

        IconButton(
            onClick = { expanded.targetState = !expanded.currentState },
            modifier = Modifier.align(Alignment.Top)
        ) {
            val resource = (if (expanded.currentState) MaterialIcon.EXPAND_LESS else MaterialIcon.EXPAND_MORE).resource
            Icon(painterResource(resource), "expand")
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
