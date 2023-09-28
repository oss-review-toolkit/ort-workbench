package org.ossreviewtoolkit.workbench.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.seanproctor.datatable.DataColumn
import com.seanproctor.datatable.material.DataTable

/**
 * A [DataTable] with two columns.
 */
@Composable
fun TwoColumnTable(headers: Pair<String, String>, data: Map<String, String>) {
    DataTable(
        modifier = Modifier.fillMaxWidth(),
        headerHeight = 36.dp,
        rowHeight = 24.dp,
        columns = listOf(
            DataColumn { SingleLineText(headers.first) },
            DataColumn { SingleLineText(headers.second) }
        )
    ) {
        data.forEach { (key, value) ->
            row {
                cell { SingleLineText(key) }
                cell { SingleLineText(value) }
            }
        }
    }
}
