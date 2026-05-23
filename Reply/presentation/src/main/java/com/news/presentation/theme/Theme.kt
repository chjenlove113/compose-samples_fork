package com.news.presentation.theme

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


private val lightScheme = lightColorScheme(
    primary = com.news.presentation.theme.primaryLight,
    onPrimary = com.news.presentation.theme.onPrimaryLight,
    primaryContainer = com.news.presentation.theme.primaryContainerLight,
    onPrimaryContainer = com.news.presentation.theme.onPrimaryContainerLight,
    secondary = com.news.presentation.theme.secondaryLight,
    onSecondary = com.news.presentation.theme.onSecondaryLight,
    secondaryContainer = com.news.presentation.theme.secondaryContainerLight,
    onSecondaryContainer = com.news.presentation.theme.onSecondaryContainerLight,
    tertiary = com.news.presentation.theme.tertiaryLight,
    onTertiary = com.news.presentation.theme.onTertiaryLight,
    tertiaryContainer = com.news.presentation.theme.tertiaryContainerLight,
    onTertiaryContainer = com.news.presentation.theme.onTertiaryContainerLight,
    error = com.news.presentation.theme.errorLight,
    onError = com.news.presentation.theme.onErrorLight,
    errorContainer = com.news.presentation.theme.errorContainerLight,
    onErrorContainer = com.news.presentation.theme.onErrorContainerLight,
    background = com.news.presentation.theme.backgroundLight,
    onBackground = com.news.presentation.theme.onBackgroundLight,
    surface = com.news.presentation.theme.surfaceLight,
    onSurface = com.news.presentation.theme.onSurfaceLight,
    surfaceVariant = com.news.presentation.theme.surfaceVariantLight,
    onSurfaceVariant = com.news.presentation.theme.onSurfaceVariantLight,
    outline = com.news.presentation.theme.outlineLight,
    outlineVariant = com.news.presentation.theme.outlineVariantLight,
    scrim = com.news.presentation.theme.scrimLight,
    inverseSurface = com.news.presentation.theme.inverseSurfaceLight,
    inverseOnSurface = com.news.presentation.theme.inverseOnSurfaceLight,
    inversePrimary = com.news.presentation.theme.inversePrimaryLight,
    surfaceDim = com.news.presentation.theme.surfaceDimLight,
    surfaceBright = com.news.presentation.theme.surfaceBrightLight,
    surfaceContainerLowest = com.news.presentation.theme.surfaceContainerLowestLight,
    surfaceContainerLow = com.news.presentation.theme.surfaceContainerLowLight,
    surfaceContainer = com.news.presentation.theme.surfaceContainerLight,
    surfaceContainerHigh = com.news.presentation.theme.surfaceContainerHighLight,
    surfaceContainerHighest = com.news.presentation.theme.surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = com.news.presentation.theme.primaryDark,
    onPrimary = com.news.presentation.theme.onPrimaryDark,
    primaryContainer = com.news.presentation.theme.primaryContainerDark,
    onPrimaryContainer = com.news.presentation.theme.onPrimaryContainerDark,
    secondary = com.news.presentation.theme.secondaryDark,
    onSecondary = com.news.presentation.theme.onSecondaryDark,
    secondaryContainer = com.news.presentation.theme.secondaryContainerDark,
    onSecondaryContainer = com.news.presentation.theme.onSecondaryContainerDark,
    tertiary = com.news.presentation.theme.tertiaryDark,
    onTertiary = com.news.presentation.theme.onTertiaryDark,
    tertiaryContainer = com.news.presentation.theme.tertiaryContainerDark,
    onTertiaryContainer = com.news.presentation.theme.onTertiaryContainerDark,
    error = com.news.presentation.theme.errorDark,
    onError = com.news.presentation.theme.onErrorDark,
    errorContainer = com.news.presentation.theme.errorContainerDark,
    onErrorContainer = com.news.presentation.theme.onErrorContainerDark,
    background = com.news.presentation.theme.backgroundDark,
    onBackground = com.news.presentation.theme.onBackgroundDark,
    surface = com.news.presentation.theme.surfaceDark,
    onSurface = com.news.presentation.theme.onSurfaceDark,
    surfaceVariant = com.news.presentation.theme.surfaceVariantDark,
    onSurfaceVariant = com.news.presentation.theme.onSurfaceVariantDark,
    outline = com.news.presentation.theme.outlineDark,
    outlineVariant = com.news.presentation.theme.outlineVariantDark,
    scrim = com.news.presentation.theme.scrimDark,
    inverseSurface = com.news.presentation.theme.inverseSurfaceDark,
    inverseOnSurface = com.news.presentation.theme.inverseOnSurfaceDark,
    inversePrimary = com.news.presentation.theme.inversePrimaryDark,
    surfaceDim = com.news.presentation.theme.surfaceDimDark,
    surfaceBright = com.news.presentation.theme.surfaceBrightDark,
    surfaceContainerLowest = com.news.presentation.theme.surfaceContainerLowestDark,
    surfaceContainerLow = com.news.presentation.theme.surfaceContainerLowDark,
    surfaceContainer = com.news.presentation.theme.surfaceContainerDark,
    surfaceContainerHigh = com.news.presentation.theme.surfaceContainerHighDark,
    surfaceContainerHighest = com.news.presentation.theme.surfaceContainerHighestDark,
)

private val mediumContrastLightColorScheme = lightColorScheme(
    primary = com.news.presentation.theme.primaryLightMediumContrast,
    onPrimary = com.news.presentation.theme.onPrimaryLightMediumContrast,
    primaryContainer = com.news.presentation.theme.primaryContainerLightMediumContrast,
    onPrimaryContainer = com.news.presentation.theme.onPrimaryContainerLightMediumContrast,
    secondary = com.news.presentation.theme.secondaryLightMediumContrast,
    onSecondary = com.news.presentation.theme.onSecondaryLightMediumContrast,
    secondaryContainer = com.news.presentation.theme.secondaryContainerLightMediumContrast,
    onSecondaryContainer = com.news.presentation.theme.onSecondaryContainerLightMediumContrast,
    tertiary = com.news.presentation.theme.tertiaryLightMediumContrast,
    onTertiary = com.news.presentation.theme.onTertiaryLightMediumContrast,
    tertiaryContainer = com.news.presentation.theme.tertiaryContainerLightMediumContrast,
    onTertiaryContainer = com.news.presentation.theme.onTertiaryContainerLightMediumContrast,
    error = com.news.presentation.theme.errorLightMediumContrast,
    onError = com.news.presentation.theme.onErrorLightMediumContrast,
    errorContainer = com.news.presentation.theme.errorContainerLightMediumContrast,
    onErrorContainer = com.news.presentation.theme.onErrorContainerLightMediumContrast,
    background = com.news.presentation.theme.backgroundLightMediumContrast,
    onBackground = com.news.presentation.theme.onBackgroundLightMediumContrast,
    surface = com.news.presentation.theme.surfaceLightMediumContrast,
    onSurface = com.news.presentation.theme.onSurfaceLightMediumContrast,
    surfaceVariant = com.news.presentation.theme.surfaceVariantLightMediumContrast,
    onSurfaceVariant = com.news.presentation.theme.onSurfaceVariantLightMediumContrast,
    outline = com.news.presentation.theme.outlineLightMediumContrast,
    outlineVariant = com.news.presentation.theme.outlineVariantLightMediumContrast,
    scrim = com.news.presentation.theme.scrimLightMediumContrast,
    inverseSurface = com.news.presentation.theme.inverseSurfaceLightMediumContrast,
    inverseOnSurface = com.news.presentation.theme.inverseOnSurfaceLightMediumContrast,
    inversePrimary = com.news.presentation.theme.inversePrimaryLightMediumContrast,
    surfaceDim = com.news.presentation.theme.surfaceDimLightMediumContrast,
    surfaceBright = com.news.presentation.theme.surfaceBrightLightMediumContrast,
    surfaceContainerLowest = com.news.presentation.theme.surfaceContainerLowestLightMediumContrast,
    surfaceContainerLow = com.news.presentation.theme.surfaceContainerLowLightMediumContrast,
    surfaceContainer = com.news.presentation.theme.surfaceContainerLightMediumContrast,
    surfaceContainerHigh = com.news.presentation.theme.surfaceContainerHighLightMediumContrast,
    surfaceContainerHighest = com.news.presentation.theme.surfaceContainerHighestLightMediumContrast,
)

private val highContrastLightColorScheme = lightColorScheme(
    primary = com.news.presentation.theme.primaryLightHighContrast,
    onPrimary = com.news.presentation.theme.onPrimaryLightHighContrast,
    primaryContainer = com.news.presentation.theme.primaryContainerLightHighContrast,
    onPrimaryContainer = com.news.presentation.theme.onPrimaryContainerLightHighContrast,
    secondary = com.news.presentation.theme.secondaryLightHighContrast,
    onSecondary = com.news.presentation.theme.onSecondaryLightHighContrast,
    secondaryContainer = com.news.presentation.theme.secondaryContainerLightHighContrast,
    onSecondaryContainer = com.news.presentation.theme.onSecondaryContainerLightHighContrast,
    tertiary = com.news.presentation.theme.tertiaryLightHighContrast,
    onTertiary = com.news.presentation.theme.onTertiaryLightHighContrast,
    tertiaryContainer = com.news.presentation.theme.tertiaryContainerLightHighContrast,
    onTertiaryContainer = com.news.presentation.theme.onTertiaryContainerLightHighContrast,
    error = com.news.presentation.theme.errorLightHighContrast,
    onError = com.news.presentation.theme.onErrorLightHighContrast,
    errorContainer = com.news.presentation.theme.errorContainerLightHighContrast,
    onErrorContainer = com.news.presentation.theme.onErrorContainerLightHighContrast,
    background = com.news.presentation.theme.backgroundLightHighContrast,
    onBackground = com.news.presentation.theme.onBackgroundLightHighContrast,
    surface = com.news.presentation.theme.surfaceLightHighContrast,
    onSurface = com.news.presentation.theme.onSurfaceLightHighContrast,
    surfaceVariant = com.news.presentation.theme.surfaceVariantLightHighContrast,
    onSurfaceVariant = com.news.presentation.theme.onSurfaceVariantLightHighContrast,
    outline = com.news.presentation.theme.outlineLightHighContrast,
    outlineVariant = com.news.presentation.theme.outlineVariantLightHighContrast,
    scrim = com.news.presentation.theme.scrimLightHighContrast,
    inverseSurface = com.news.presentation.theme.inverseSurfaceLightHighContrast,
    inverseOnSurface = com.news.presentation.theme.inverseOnSurfaceLightHighContrast,
    inversePrimary = com.news.presentation.theme.inversePrimaryLightHighContrast,
    surfaceDim = com.news.presentation.theme.surfaceDimLightHighContrast,
    surfaceBright = com.news.presentation.theme.surfaceBrightLightHighContrast,
    surfaceContainerLowest = com.news.presentation.theme.surfaceContainerLowestLightHighContrast,
    surfaceContainerLow = com.news.presentation.theme.surfaceContainerLowLightHighContrast,
    surfaceContainer = com.news.presentation.theme.surfaceContainerLightHighContrast,
    surfaceContainerHigh = com.news.presentation.theme.surfaceContainerHighLightHighContrast,
    surfaceContainerHighest = com.news.presentation.theme.surfaceContainerHighestLightHighContrast,
)

private val mediumContrastDarkColorScheme = darkColorScheme(
    primary = com.news.presentation.theme.primaryDarkMediumContrast,
    onPrimary = com.news.presentation.theme.onPrimaryDarkMediumContrast,
    primaryContainer = com.news.presentation.theme.primaryContainerDarkMediumContrast,
    onPrimaryContainer = com.news.presentation.theme.onPrimaryContainerDarkMediumContrast,
    secondary = com.news.presentation.theme.secondaryDarkMediumContrast,
    onSecondary = com.news.presentation.theme.onSecondaryDarkMediumContrast,
    secondaryContainer = com.news.presentation.theme.secondaryContainerDarkMediumContrast,
    onSecondaryContainer = com.news.presentation.theme.onSecondaryContainerDarkMediumContrast,
    tertiary = com.news.presentation.theme.tertiaryDarkMediumContrast,
    onTertiary = com.news.presentation.theme.onTertiaryDarkMediumContrast,
    tertiaryContainer = com.news.presentation.theme.tertiaryContainerDarkMediumContrast,
    onTertiaryContainer = com.news.presentation.theme.onTertiaryContainerDarkMediumContrast,
    error = com.news.presentation.theme.errorDarkMediumContrast,
    onError = com.news.presentation.theme.onErrorDarkMediumContrast,
    errorContainer = com.news.presentation.theme.errorContainerDarkMediumContrast,
    onErrorContainer = com.news.presentation.theme.onErrorContainerDarkMediumContrast,
    background = com.news.presentation.theme.backgroundDarkMediumContrast,
    onBackground = com.news.presentation.theme.onBackgroundDarkMediumContrast,
    surface = com.news.presentation.theme.surfaceDarkMediumContrast,
    onSurface = com.news.presentation.theme.onSurfaceDarkMediumContrast,
    surfaceVariant = com.news.presentation.theme.surfaceVariantDarkMediumContrast,
    onSurfaceVariant = com.news.presentation.theme.onSurfaceVariantDarkMediumContrast,
    outline = com.news.presentation.theme.outlineDarkMediumContrast,
    outlineVariant = com.news.presentation.theme.outlineVariantDarkMediumContrast,
    scrim = com.news.presentation.theme.scrimDarkMediumContrast,
    inverseSurface = com.news.presentation.theme.inverseSurfaceDarkMediumContrast,
    inverseOnSurface = com.news.presentation.theme.inverseOnSurfaceDarkMediumContrast,
    inversePrimary = com.news.presentation.theme.inversePrimaryDarkMediumContrast,
    surfaceDim = com.news.presentation.theme.surfaceDimDarkMediumContrast,
    surfaceBright = com.news.presentation.theme.surfaceBrightDarkMediumContrast,
    surfaceContainerLowest = com.news.presentation.theme.surfaceContainerLowestDarkMediumContrast,
    surfaceContainerLow = com.news.presentation.theme.surfaceContainerLowDarkMediumContrast,
    surfaceContainer = com.news.presentation.theme.surfaceContainerDarkMediumContrast,
    surfaceContainerHigh = com.news.presentation.theme.surfaceContainerHighDarkMediumContrast,
    surfaceContainerHighest = com.news.presentation.theme.surfaceContainerHighestDarkMediumContrast,
)

private val highContrastDarkColorScheme = darkColorScheme(
    primary = com.news.presentation.theme.primaryDarkHighContrast,
    onPrimary = com.news.presentation.theme.onPrimaryDarkHighContrast,
    primaryContainer = com.news.presentation.theme.primaryContainerDarkHighContrast,
    onPrimaryContainer = com.news.presentation.theme.onPrimaryContainerDarkHighContrast,
    secondary = com.news.presentation.theme.secondaryDarkHighContrast,
    onSecondary = com.news.presentation.theme.onSecondaryDarkHighContrast,
    secondaryContainer = com.news.presentation.theme.secondaryContainerDarkHighContrast,
    onSecondaryContainer = com.news.presentation.theme.onSecondaryContainerDarkHighContrast,
    tertiary = com.news.presentation.theme.tertiaryDarkHighContrast,
    onTertiary = com.news.presentation.theme.onTertiaryDarkHighContrast,
    tertiaryContainer = com.news.presentation.theme.tertiaryContainerDarkHighContrast,
    onTertiaryContainer = com.news.presentation.theme.onTertiaryContainerDarkHighContrast,
    error = com.news.presentation.theme.errorDarkHighContrast,
    onError = com.news.presentation.theme.onErrorDarkHighContrast,
    errorContainer = com.news.presentation.theme.errorContainerDarkHighContrast,
    onErrorContainer = com.news.presentation.theme.onErrorContainerDarkHighContrast,
    background = com.news.presentation.theme.backgroundDarkHighContrast,
    onBackground = com.news.presentation.theme.onBackgroundDarkHighContrast,
    surface = com.news.presentation.theme.surfaceDarkHighContrast,
    onSurface = com.news.presentation.theme.onSurfaceDarkHighContrast,
    surfaceVariant = com.news.presentation.theme.surfaceVariantDarkHighContrast,
    onSurfaceVariant = com.news.presentation.theme.onSurfaceVariantDarkHighContrast,
    outline = com.news.presentation.theme.outlineDarkHighContrast,
    outlineVariant = com.news.presentation.theme.outlineVariantDarkHighContrast,
    scrim = com.news.presentation.theme.scrimDarkHighContrast,
    inverseSurface = com.news.presentation.theme.inverseSurfaceDarkHighContrast,
    inverseOnSurface = com.news.presentation.theme.inverseOnSurfaceDarkHighContrast,
    inversePrimary = com.news.presentation.theme.inversePrimaryDarkHighContrast,
    surfaceDim = com.news.presentation.theme.surfaceDimDarkHighContrast,
    surfaceBright = com.news.presentation.theme.surfaceBrightDarkHighContrast,
    surfaceContainerLowest = com.news.presentation.theme.surfaceContainerLowestDarkHighContrast,
    surfaceContainerLow = com.news.presentation.theme.surfaceContainerLowDarkHighContrast,
    surfaceContainer = com.news.presentation.theme.surfaceContainerDarkHighContrast,
    surfaceContainerHigh = com.news.presentation.theme.surfaceContainerHighDarkHighContrast,
    surfaceContainerHighest = com.news.presentation.theme.surfaceContainerHighestDarkHighContrast,
)

fun isContrastAvailable(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
}

@Composable
fun selectSchemeForContrast(isDark: Boolean,): ColorScheme {
    val context = LocalContext.current
    var colorScheme = if (isDark) darkScheme else lightScheme
    val isPreview = LocalInspectionMode.current
    // TODO(b/336693596): UIModeManager is not yet supported in preview
    if (!isPreview && isContrastAvailable()) {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        val contrastLevel = uiModeManager.contrast

        colorScheme = when (contrastLevel) {
            in 0.0f..0.33f -> if (isDark)
                darkScheme else lightScheme

            in 0.34f..0.66f -> if (isDark)
                mediumContrastDarkColorScheme else mediumContrastLightColorScheme

            in 0.67f..1.0f -> if (isDark)
                highContrastDarkColorScheme else highContrastLightColorScheme

            else -> if (isDark) darkScheme else lightScheme
        }
        return colorScheme
    } else return colorScheme
}
@Composable
fun ContrastAwareReplyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    fontScale: Float = 1.0f,
    content: @Composable() () -> Unit
) {
    val replyColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> selectSchemeForContrast(darkTheme)
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = replyColorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = replyColorScheme,
        typography = replyTypography.scaled(fontScale),
        shapes = com.news.presentation.theme.shapes,
        content = content
    )
}
