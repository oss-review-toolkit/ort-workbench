package org.ossreviewtoolkit.workbench.ui.packagedetails

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.workbench.composables.ScreenAppBar
import org.ossreviewtoolkit.workbench.utils.MaterialIcon

@Composable
fun PackageDetails(id: Identifier, onBack: () -> Unit) {
    ScreenAppBar(
        title = { Text(id.toCoordinates(), style = MaterialTheme.typography.h3) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painterResource(MaterialIcon.ARROW_BACK.resource),
                    contentDescription = "Back",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    )
}
