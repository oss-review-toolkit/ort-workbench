package org.ossreviewtoolkit.workbench.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

/**
 * Display the [text] on a single line using [TextOverflow.Ellipsis] on overflow. The text is also shown in a [Tooltip]
 * on mouse over.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SingleLineText(text: String) {
    TooltipArea(tooltip = { Tooltip(text) }) {
        Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
