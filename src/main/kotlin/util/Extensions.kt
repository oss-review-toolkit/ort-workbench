package org.ossreviewtoolkit.workbench.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import org.ossreviewtoolkit.utils.common.titlecase

@Composable
fun Modifier.conditional(condition: Boolean, modifier: @Composable Modifier.() -> Modifier) =
    if (condition) then(modifier(Modifier)) else this

fun String.enumcase() = replace("_", " ").titlecase()
