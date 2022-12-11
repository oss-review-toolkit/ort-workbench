package org.ossreviewtoolkit.workbench.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun TopBar() {
    TopAppBar(modifier = Modifier.zIndex(zIndex = 5f), backgroundColor = MaterialTheme.colors.primaryVariant) {
        Image(
            painter = painterResource("ort-white.png"),
            contentDescription = "OSS Review Toolkit",
            contentScale = ContentScale.FillHeight,
            modifier = Modifier.padding(vertical = 10.dp).width(200.dp)
        )
    }
}
