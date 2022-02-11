
package org.ossreviewtoolkit.workbench.util

import java.awt.Desktop
import java.io.File
import java.net.URI

fun browseDirectory(file: File) {
    Desktop.getDesktop().apply {
        runCatching {
            if (isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
                browseFileDirectory(file)
            } else if (isSupported(Desktop.Action.OPEN)) {
                open(file)
            }
        }.onFailure {
            // TODO: Propagate error.
        }
    }
}

fun editFile(file: File) {
    Desktop.getDesktop().apply {
        runCatching {
            if (isSupported(Desktop.Action.EDIT)) {
                edit(file)
            }
        }.recoverCatching {
            if (isSupported(Desktop.Action.OPEN)) {
                open(file)
            }
        }.onFailure {
            // TODO: Propagate error.
        }
    }
}

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
