package com.nxzef.wc.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import wc.composeapp.generated.resources.Poppins_Black
import wc.composeapp.generated.resources.Poppins_Bold
import wc.composeapp.generated.resources.Poppins_ExtraBold
import wc.composeapp.generated.resources.Poppins_ExtraLight
import wc.composeapp.generated.resources.Poppins_Light
import wc.composeapp.generated.resources.Poppins_Medium
import wc.composeapp.generated.resources.Poppins_Regular
import wc.composeapp.generated.resources.Poppins_SemiBold
import wc.composeapp.generated.resources.Poppins_Thin
import wc.composeapp.generated.resources.Res

/**
 * Loads the 'Poppins' font family with all its weights.
 * This is used as the primary typeface for the application to ensure a modern
 * and readable look across all platforms.
 */
@Composable
private fun poppinsFontFamily() = FontFamily(
    Font(Res.font.Poppins_Thin, FontWeight.Thin),
    Font(Res.font.Poppins_ExtraLight, FontWeight.ExtraLight),
    Font(Res.font.Poppins_Light, FontWeight.Light),
    Font(Res.font.Poppins_Regular, FontWeight.Normal),
    Font(Res.font.Poppins_Medium, FontWeight.Medium),
    Font(Res.font.Poppins_SemiBold, FontWeight.SemiBold),
    Font(Res.font.Poppins_Bold, FontWeight.Bold),
    Font(Res.font.Poppins_ExtraBold, FontWeight.ExtraBold),
    Font(Res.font.Poppins_Black, FontWeight.Black)
)

/**
 * Returns the customized [Typography] for the application.
 *
 * This function overrides the default Material 3 typography by applying the Poppins
 * font family to all text styles (Display, Headline, Title, Body, and Label).
 */
@Composable
fun typography(): Typography {
    val poppins = poppinsFontFamily()
    return Typography().run {
        copy(
            displayLarge = displayLarge.copy(fontFamily = poppins),
            displayMedium = displayMedium.copy(fontFamily = poppins),
            displaySmall = displaySmall.copy(fontFamily = poppins),
            headlineLarge = headlineLarge.copy(fontFamily = poppins),
            headlineMedium = headlineMedium.copy(fontFamily = poppins),
            headlineSmall = headlineSmall.copy(fontFamily = poppins),
            titleLarge = titleLarge.copy(fontFamily = poppins),
            titleMedium = titleMedium.copy(fontFamily = poppins),
            titleSmall = titleSmall.copy(fontFamily = poppins),
            bodyLarge = bodyLarge.copy(fontFamily = poppins),
            bodyMedium = bodyMedium.copy(fontFamily = poppins),
            bodySmall = bodySmall.copy(fontFamily = poppins),
            labelLarge = labelLarge.copy(fontFamily = poppins),
            labelMedium = labelMedium.copy(fontFamily = poppins),
            labelSmall = labelSmall.copy(fontFamily = poppins)
        )
    }
}
