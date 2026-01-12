package org.ossreviewtoolkit.workbench.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.workbench.theme.Error
import org.ossreviewtoolkit.workbench.theme.Hint
import org.ossreviewtoolkit.workbench.theme.LightGray
import org.ossreviewtoolkit.workbench.theme.Warning

@Composable
fun SeverityIcon(severity: Severity, resolved: Boolean = false, size: Dp = 24.dp) {
    val icon = when (severity) {
        Severity.HINT -> Icons.Default.Info
        Severity.WARNING -> Icons.Default.Warning
        Severity.ERROR -> Icons.Default.Error
    }

    val tint = if (resolved) {
        LightGray
    } else {
        when (severity) {
            Severity.HINT -> Hint
            Severity.WARNING -> Warning
            Severity.ERROR -> Error
        }
    }

    Icon(
        icon,
        contentDescription = severity.name,
        tint = tint,
        modifier = Modifier.size(size)
    )
}

@Composable
@Preview
private fun SeverityIconPreview() {
    Preview {
        Row {
            SeverityIcon(Severity.HINT)
            SeverityIcon(Severity.WARNING)
            SeverityIcon(Severity.ERROR)
            SeverityIcon(Severity.HINT, resolved = true)
            SeverityIcon(Severity.WARNING, resolved = true)
            SeverityIcon(Severity.ERROR, resolved = true)
        }
    }
}
