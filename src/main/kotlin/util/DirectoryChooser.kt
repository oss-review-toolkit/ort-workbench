package org.ossreviewtoolkit.workbench.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

import java.io.File

import javax.swing.JFileChooser
import javax.swing.UIManager

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun DirectoryChooser(currentDirectory: File? = null, onResult: (result: File?) -> Unit) {
    DisposableEffect(Unit) {
        val job = GlobalScope.launch(Dispatchers.Swing) {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
            val fileChooser = JFileChooser()
            fileChooser.currentDirectory = currentDirectory
            fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                onResult(fileChooser.selectedFile)
            } else {
                onResult(null)
            }
        }

        onDispose {
            job.cancel()
        }
    }
}
