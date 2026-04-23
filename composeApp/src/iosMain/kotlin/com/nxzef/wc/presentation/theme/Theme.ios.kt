package com.nxzef.wc.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
actual fun WCTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    content: @Composable (() -> Unit)
) {
    val colorScheme = if (darkTheme) darkScheme else lightScheme

    val wcColors = if (darkTheme) darkWCColors else lightWCColors

    CompositionLocalProvider(
        LocalWCColors provides wcColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography(),
            content = content
        )
    }
}
