package com.nxzef.wc.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
actual fun WCTheme(
    appTheme: AppTheme,
    dynamicColor: Boolean,
    content: @Composable (() -> Unit)
) {
    val darkTheme = when (appTheme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) darkScheme else lightScheme
    val wcColors = if (darkTheme) darkWCColors else lightWCColors

    CompositionLocalProvider(LocalWCColors provides wcColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography(),
            content = content
        )
    }
}
