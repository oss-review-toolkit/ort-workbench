package org.ossreviewtoolkit.workbench.composables

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex

@Composable
fun ScreenAppBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        modifier = Modifier.zIndex(1f),
        backgroundColor = MaterialTheme.colors.primary,
        title = title,
        navigationIcon = navigationIcon,
        actions = actions
    )
}
