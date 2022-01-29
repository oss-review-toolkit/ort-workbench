package org.ossreviewtoolkit.workbench.util

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource

import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.workbench.theme.Error
import org.ossreviewtoolkit.workbench.theme.Hint
import org.ossreviewtoolkit.workbench.theme.LightGray
import org.ossreviewtoolkit.workbench.theme.Warning

@Composable
fun SeverityIcon(severity: Severity, resolved: Boolean = false) {
    val icon = when (severity) {
        Severity.HINT -> MaterialIcon.INFO
        Severity.WARNING -> MaterialIcon.WARNING
        Severity.ERROR -> MaterialIcon.ERROR
    }

    val tint = if (resolved) LightGray else when (severity) {
        Severity.HINT -> Hint
        Severity.WARNING -> Warning
        Severity.ERROR -> Error
    }

    Icon(painterResource(icon.resource), contentDescription = severity.name, tint = tint)
}

@Composable
@Preview
private fun SeverityIconPreview() {
    Preview {
        Row {
            SeverityIcon(Severity.HINT)
            SeverityIcon(Severity.WARNING)
            SeverityIcon(Severity.ERROR)
        }
    }
}
