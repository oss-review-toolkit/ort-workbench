package org.ossreviewtoolkit.workbench.composables

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <ITEM> ListScreenList(
    items: List<ITEM>,
    itemsEmptyText: String,
    item: @Composable LazyItemScope.(item: ITEM) -> Unit
) {
    if (items.isEmpty()) {
        Text(itemsEmptyText, modifier = Modifier.padding(15.dp))
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            val listState = rememberLazyListState()

            LazyColumn(
                contentPadding = PaddingValues(15.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                state = listState
            ) {
                items(items.size, key = { it }) { index ->
                    item(items[index])
                }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState = listState)
            )
        }
    }
}
