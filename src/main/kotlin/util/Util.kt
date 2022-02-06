package org.ossreviewtoolkit.workbench.util

import java.awt.Desktop
import java.net.URI

fun openUrlInBrowser(url: String) {
    runCatching {
        val uri = URI(url)
        Desktop.getDesktop().apply {
            if (isSupported(Desktop.Action.BROWSE)) {
                browse(uri)
            }
        }
    }
}
