package com.nxzef.wc.platform

actual fun openUrl(url: String) {
    try {
        java.awt.Desktop.getDesktop().browse(java.net.URI(url))
    } catch (_: Exception) { }
}
