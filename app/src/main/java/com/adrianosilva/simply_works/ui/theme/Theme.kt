package com.adrianosilva.simply_works.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
        darkColorScheme(
                primary = CyanAccent,
                secondary = CyanDim,
                tertiary = WarningAmber,
                background = PrimaryNavy,
                surface = SecondaryNavy,
                onPrimary = PrimaryNavy,
                onSecondary = SoftWhite,
                onTertiary = PrimaryNavy,
                onBackground = SoftWhite,
                onSurface = SoftWhite,
                error = ErrorRed
        )

private val LightColorScheme =
        lightColorScheme(
                primary = CyanAccent,
                secondary = CyanDim,
                tertiary = WarningAmber,
                background =
                        PrimaryNavy, // Force dark theme look even in light mode for consistency
                surface = SecondaryNavy,
                onPrimary = PrimaryNavy,
                onSecondary = SoftWhite,
                onTertiary = PrimaryNavy,
                onBackground = SoftWhite,
                onSurface = SoftWhite,
                error = ErrorRed
        )

@Composable
fun SimplyworksTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        // Dynamic color is available on Android 12+
        dynamicColor: Boolean = false, // Disable dynamic color to enforce our premium brand
        content: @Composable () -> Unit
) {
    val colorScheme =
            when {
                // dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                //     val context = LocalContext.current
                //     if (darkTheme) dynamicDarkColorScheme(context) else
                // dynamicLightColorScheme(context)
                // }
                // Force the same scheme for now to guarantee the look
                else -> DarkColorScheme
            }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
