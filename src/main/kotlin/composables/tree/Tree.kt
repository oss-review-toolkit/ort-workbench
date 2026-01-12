package org.ossreviewtoolkit.workbench.composables.tree

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import org.ossreviewtoolkit.workbench.composables.Preview

@Composable
fun <VALUE> Tree(
    modifier: Modifier = Modifier,
    roots: List<TreeNode<VALUE>>,
    startExpanded: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
    indentation: Dp = 5.dp,
    expandIcon: ImageVector = Icons.Default.ChevronRight,
    expandedIcon: ImageVector = Icons.Default.ExpandMore,
    iconSize: Dp = 12.dp,
    itemContent: @Composable (item: TreeItem<VALUE>, isSelected: Boolean) -> Unit = { item, isSelected ->
        DefaultItemContent(item, isSelected)
    }
) {
    Tree(
        modifier = modifier,
        state = TreeState(roots, startExpanded),
        listState = listState,
        indentation = indentation,
        expandIcon = expandIcon,
        expandedIcon = expandedIcon,
        iconSize = iconSize,
        itemContent = itemContent
    )
}

@Composable
fun <VALUE> Tree(
    modifier: Modifier = Modifier,
    state: TreeState<VALUE>,
    listState: LazyListState = rememberLazyListState(),
    indentation: Dp = 5.dp,
    expandIcon: ImageVector = Icons.Default.ChevronRight,
    expandedIcon: ImageVector = Icons.Default.ExpandMore,
    iconSize: Dp = 12.dp,
    itemContent: @Composable (item: TreeItem<VALUE>, isSelected: Boolean) -> Unit = { item, isSelected ->
        DefaultItemContent(item, isSelected)
    }
) {
    fun handleKeyEvent(event: KeyEvent) =
        when (event.type) {
            KeyEventType.KeyUp -> {
                when (event.key) {
                    Key.DirectionDown -> {
                        state.selectNextItem()
                        true
                    }

                    Key.DirectionUp -> {
                        state.selectPreviousItem()
                        true
                    }

                    Key.DirectionLeft -> {
                        state.collapseSelectedItem()
                        true
                    }

                    Key.DirectionRight -> {
                        state.expandSelectedItem()
                        true
                    }

                    else -> false
                }
            }

            else -> false
        }

    if (state.isItemAutoSelected) {
        val selectedItemIndex = state.visibleItems.indexOf(state.selectedItem)
        if (selectedItemIndex >= 0) {
            LaunchedEffect(selectedItemIndex) {
                listState.animateScrollToItem(selectedItemIndex)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.onKeyEvent(::handleKeyEvent),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        items(state.visibleItems.size, key = { state.visibleItems[it].key }) {
            val item = state.visibleItems[it]
            Row(
                modifier = Modifier.padding(start = (indentation * item.level)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = when {
                    item.node.children.isNotEmpty() -> if (item.expanded) expandedIcon else expandIcon
                    else -> null
                }

                if (icon != null) {
                    Icon(
                        painter = rememberVectorPainter(icon),
                        contentDescription = null,
                        modifier = Modifier.size(iconSize).clickable { state.toggleExpanded(item) }
                    )
                }

                Box(
                    modifier = Modifier.padding(start = if (icon == null) iconSize else 0.dp)
                        .clickable { state.selectItem(item, isAutoSelected = false) }
                ) {
                    itemContent(item, item.index == state.selectedItem?.index)
                }
            }
        }
    }
}

@Composable
private fun <VALUE> DefaultItemContent(item: TreeItem<VALUE>, isSelected: Boolean) {
    Text(text = item.node.value.toString(), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
}

@Composable
@Preview
fun TreePreview() {
    val roots = listOf(
        TreeNode(
            value = "Root",
            key = "Root",
            children = listOf(
                TreeNode(
                    value = "Child 1",
                    key = "Child 1",
                    children = listOf(
                        TreeNode(value = "Child 1.1", key = "Child 1.1"),
                        TreeNode(
                            value = "Child 1.2",
                            key = "Child 1.2",
                            children = listOf(
                                TreeNode(value = "Child 1.2.1", key = "Child 1.2.1")
                            )
                        ),
                        TreeNode(value = "Child 1.3", key = "Child 1.3")
                    )
                ),
                TreeNode(
                    value = "Child 2",
                    key = "Child 2",
                    children = listOf(
                        TreeNode(value = "Child 2.1", key = "Child 2.1"),
                        TreeNode(value = "Child 2.2", key = "Child 2.2")
                    )
                )
            )
        ),
        TreeNode(
            value = "Root 2",
            key = "Root 2",
            children = listOf(
                TreeNode(value = "Child 1", key = "Child 1"),
                TreeNode(value = "Child 2", key = "Child 2")
            )
        )
    )

    Preview {
        Tree(roots = roots, startExpanded = true)
    }
}
