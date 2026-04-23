package com.nxzef.wc.presentation.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun WCTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    content: @Composable (() -> Unit)
) {
    val colorScheme = when {
        // Use Material You dynamic color if supported and requested
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        // Use predefined Dark or Light schemes
        darkTheme -> darkScheme
        else -> lightScheme
    }

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
