package com.nxzef.wc

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.nxzef.wc.di.initKoin

fun main() {
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
            App()
        }
    }
}