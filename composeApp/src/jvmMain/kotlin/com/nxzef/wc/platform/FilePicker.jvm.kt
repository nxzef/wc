package com.nxzef.wc.platform

import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual fun pickPdfFile(): Triple<String, String, ByteArray>? {
    val chooser = JFileChooser().apply {
        fileFilter = FileNameExtensionFilter("PDF Files", "pdf")
        dialogTitle = "Select Quote PDF"
    }
    val result = chooser.showOpenDialog(null)
    return if (result == JFileChooser.APPROVE_OPTION) {
        val file = chooser.selectedFile
        Triple(file.absolutePath, file.name, file.readBytes())
    } else null
}
