package com.nxzef.wc

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.nxzef.wc.di.initKoin

fun main() {
    // Prevent the white AWT frame flash before Compose paints its first frame
    javax.swing.UIManager.put("Panel.background", java.awt.Color(0x0E, 0x15, 0x13))

    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "The Wedding Clouds",
            state = rememberWindowState(
                width = 1280.dp,
                height = 800.dp
            )
        ) {
            window.background = java.awt.Color(0x0E, 0x15, 0x13)
            App()
        }
    }
}