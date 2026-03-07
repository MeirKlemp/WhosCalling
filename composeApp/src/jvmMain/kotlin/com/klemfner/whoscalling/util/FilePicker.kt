package com.klemfner.whoscalling.util

import androidx.compose.runtime.Composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun rememberFileSaver(): suspend (fileName: String, content: String) -> Boolean {
    return { fileName, content ->
        withContext(Dispatchers.Swing) {
            val chooser = JFileChooser().apply {
                dialogTitle = "Export Contacts"
                selectedFile = File(fileName)
                fileFilter = FileNameExtensionFilter("JSON files", "json")
            }
            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                var file = chooser.selectedFile
                if (!file.name.endsWith(".json")) {
                    file = File(file.path + ".json")
                }
                file.writeText(content)
                true
            } else {
                false
            }
        }
    }
}

@Composable
actual fun rememberFileLoader(): suspend () -> String? {
    return {
        withContext(Dispatchers.Swing) {
            val chooser = JFileChooser().apply {
                dialogTitle = "Import Contacts"
                fileFilter = FileNameExtensionFilter("JSON files", "json")
            }
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                chooser.selectedFile.readText()
            } else {
                null
            }
        }
    }
}
