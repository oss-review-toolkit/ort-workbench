package org.ossreviewtoolkit.workbench.composables

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Display the provided [instant] as localized date time using the provided [formatStyle].
 */
@Composable
fun Datetime(instant: Instant, formatStyle: FormatStyle = FormatStyle.MEDIUM) {
    Text(rememberFormattedDatetime(instant, formatStyle))
}

/**
 * Format and remember the provided [instant] as a localized date time using the provided [formatStyle].
 */
@Composable
fun rememberFormattedDatetime(instant: Instant, formatStyle: FormatStyle = FormatStyle.MEDIUM): String {
    val formatter = remember(formatStyle) { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM) }
    return remember(instant) {
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        formatter.format(localDateTime)
    }
}
