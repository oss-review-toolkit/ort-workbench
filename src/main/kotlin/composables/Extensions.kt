package org.ossreviewtoolkit.workbench.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import org.ossreviewtoolkit.utils.common.titlecase

@Composable
fun Modifier.conditional(condition: Boolean, modifier: @Composable Modifier.() -> Modifier) =
    if (condition) then(modifier(Modifier)) else this

fun String.enumcase() = replace("_", " ").titlecase()

fun Any?.toStringOrDash() = this?.toString()?.takeIf { it.isNotEmpty() } ?: "-"
