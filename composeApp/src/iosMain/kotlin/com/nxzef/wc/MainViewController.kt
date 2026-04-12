package com.nxzef.wc

import androidx.compose.ui.window.ComposeUIViewController
import com.nxzef.wc.di.initKoin

fun MainViewController() = ComposeUIViewController(configure = {
    initKoin()
}) {
    App()
}