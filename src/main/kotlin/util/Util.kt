package org.ossreviewtoolkit.workbench.util

import java.awt.Desktop
import java.net.URI

fun openUrlInBrowser(url: String) {
    runCatching {
        val uri = URI(url)
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.browse(uri)
        }
    }
}
