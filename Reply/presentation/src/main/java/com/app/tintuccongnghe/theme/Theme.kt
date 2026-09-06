package com.app.tintuccongnghe.theme

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
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
    primary = com.app.tintuccongnghe.theme.primaryLight,
    onPrimary = com.app.tintuccongnghe.theme.onPrimaryLight,
    primaryContainer = com.app.tintuccongnghe.theme.primaryContainerLight,
    onPrimaryContainer = com.app.tintuccongnghe.theme.onPrimaryContainerLight,
    secondary = com.app.tintuccongnghe.theme.secondaryLight,
    onSecondary = com.app.tintuccongnghe.theme.onSecondaryLight,
    secondaryContainer = com.app.tintuccongnghe.theme.secondaryContainerLight,
    onSecondaryContainer = com.app.tintuccongnghe.theme.onSecondaryContainerLight,
    tertiary = com.app.tintuccongnghe.theme.tertiaryLight,
    onTertiary = com.app.tintuccongnghe.theme.onTertiaryLight,
    tertiaryContainer = com.app.tintuccongnghe.theme.tertiaryContainerLight,
    onTertiaryContainer = com.app.tintuccongnghe.theme.onTertiaryContainerLight,
    error = com.app.tintuccongnghe.theme.errorLight,
    onError = com.app.tintuccongnghe.theme.onErrorLight,
    errorContainer = com.app.tintuccongnghe.theme.errorContainerLight,
    onErrorContainer = com.app.tintuccongnghe.theme.onErrorContainerLight,
    background = com.app.tintuccongnghe.theme.backgroundLight,
    onBackground = com.app.tintuccongnghe.theme.onBackgroundLight,
    surface = com.app.tintuccongnghe.theme.surfaceLight,
    onSurface = com.app.tintuccongnghe.theme.onSurfaceLight,
    surfaceVariant = com.app.tintuccongnghe.theme.surfaceVariantLight,
    onSurfaceVariant = com.app.tintuccongnghe.theme.onSurfaceVariantLight,
    outline = com.app.tintuccongnghe.theme.outlineLight,
    outlineVariant = com.app.tintuccongnghe.theme.outlineVariantLight,
    scrim = com.app.tintuccongnghe.theme.scrimLight,
    inverseSurface = com.app.tintuccongnghe.theme.inverseSurfaceLight,
    inverseOnSurface = com.app.tintuccongnghe.theme.inverseOnSurfaceLight,
    inversePrimary = com.app.tintuccongnghe.theme.inversePrimaryLight,
    surfaceDim = com.app.tintuccongnghe.theme.surfaceDimLight,
    surfaceBright = com.app.tintuccongnghe.theme.surfaceBrightLight,
    surfaceContainerLowest = com.app.tintuccongnghe.theme.surfaceContainerLowestLight,
    surfaceContainerLow = com.app.tintuccongnghe.theme.surfaceContainerLowLight,
    surfaceContainer = com.app.tintuccongnghe.theme.surfaceContainerLight,
    surfaceContainerHigh = com.app.tintuccongnghe.theme.surfaceContainerHighLight,
    surfaceContainerHighest = com.app.tintuccongnghe.theme.surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = com.app.tintuccongnghe.theme.primaryDark,
    onPrimary = com.app.tintuccongnghe.theme.onPrimaryDark,
    primaryContainer = com.app.tintuccongnghe.theme.primaryContainerDark,
    onPrimaryContainer = com.app.tintuccongnghe.theme.onPrimaryContainerDark,
    secondary = com.app.tintuccongnghe.theme.secondaryDark,
    onSecondary = com.app.tintuccongnghe.theme.onSecondaryDark,
    secondaryContainer = com.app.tintuccongnghe.theme.secondaryContainerDark,
    onSecondaryContainer = com.app.tintuccongnghe.theme.onSecondaryContainerDark,
    tertiary = com.app.tintuccongnghe.theme.tertiaryDark,
    onTertiary = com.app.tintuccongnghe.theme.onTertiaryDark,
    tertiaryContainer = com.app.tintuccongnghe.theme.tertiaryContainerDark,
    onTertiaryContainer = com.app.tintuccongnghe.theme.onTertiaryContainerDark,
    error = com.app.tintuccongnghe.theme.errorDark,
    onError = com.app.tintuccongnghe.theme.onErrorDark,
    errorContainer = com.app.tintuccongnghe.theme.errorContainerDark,
    onErrorContainer = com.app.tintuccongnghe.theme.onErrorContainerDark,
    background = com.app.tintuccongnghe.theme.backgroundDark,
    onBackground = com.app.tintuccongnghe.theme.onBackgroundDark,
    surface = com.app.tintuccongnghe.theme.surfaceDark,
    onSurface = com.app.tintuccongnghe.theme.onSurfaceDark,
    surfaceVariant = com.app.tintuccongnghe.theme.surfaceVariantDark,
    onSurfaceVariant = com.app.tintuccongnghe.theme.onSurfaceVariantDark,
    outline = com.app.tintuccongnghe.theme.outlineDark,
    outlineVariant = com.app.tintuccongnghe.theme.outlineVariantDark,
    scrim = com.app.tintuccongnghe.theme.scrimDark,
    inverseSurface = com.app.tintuccongnghe.theme.inverseSurfaceDark,
    inverseOnSurface = com.app.tintuccongnghe.theme.inverseOnSurfaceDark,
    inversePrimary = com.app.tintuccongnghe.theme.inversePrimaryDark,
    surfaceDim = com.app.tintuccongnghe.theme.surfaceDimDark,
    surfaceBright = com.app.tintuccongnghe.theme.surfaceBrightDark,
    surfaceContainerLowest = com.app.tintuccongnghe.theme.surfaceContainerLowestDark,
    surfaceContainerLow = com.app.tintuccongnghe.theme.surfaceContainerLowDark,
    surfaceContainer = com.app.tintuccongnghe.theme.surfaceContainerDark,
    surfaceContainerHigh = com.app.tintuccongnghe.theme.surfaceContainerHighDark,
    surfaceContainerHighest = com.app.tintuccongnghe.theme.surfaceContainerHighestDark,
)

private val mediumContrastLightColorScheme = lightColorScheme(
    primary = com.app.tintuccongnghe.theme.primaryLightMediumContrast,
    onPrimary = com.app.tintuccongnghe.theme.onPrimaryLightMediumContrast,
    primaryContainer = com.app.tintuccongnghe.theme.primaryContainerLightMediumContrast,
    onPrimaryContainer = com.app.tintuccongnghe.theme.onPrimaryContainerLightMediumContrast,
    secondary = com.app.tintuccongnghe.theme.secondaryLightMediumContrast,
    onSecondary = com.app.tintuccongnghe.theme.onSecondaryLightMediumContrast,
    secondaryContainer = com.app.tintuccongnghe.theme.secondaryContainerLightMediumContrast,
    onSecondaryContainer = com.app.tintuccongnghe.theme.onSecondaryContainerLightMediumContrast,
    tertiary = com.app.tintuccongnghe.theme.tertiaryLightMediumContrast,
    onTertiary = com.app.tintuccongnghe.theme.onTertiaryLightMediumContrast,
    tertiaryContainer = com.app.tintuccongnghe.theme.tertiaryContainerLightMediumContrast,
    onTertiaryContainer = com.app.tintuccongnghe.theme.onTertiaryContainerLightMediumContrast,
    error = com.app.tintuccongnghe.theme.errorLightMediumContrast,
    onError = com.app.tintuccongnghe.theme.onErrorLightMediumContrast,
    errorContainer = com.app.tintuccongnghe.theme.errorContainerLightMediumContrast,
    onErrorContainer = com.app.tintuccongnghe.theme.onErrorContainerLightMediumContrast,
    background = com.app.tintuccongnghe.theme.backgroundLightMediumContrast,
    onBackground = com.app.tintuccongnghe.theme.onBackgroundLightMediumContrast,
    surface = com.app.tintuccongnghe.theme.surfaceLightMediumContrast,
    onSurface = com.app.tintuccongnghe.theme.onSurfaceLightMediumContrast,
    surfaceVariant = com.app.tintuccongnghe.theme.surfaceVariantLightMediumContrast,
    onSurfaceVariant = com.app.tintuccongnghe.theme.onSurfaceVariantLightMediumContrast,
    outline = com.app.tintuccongnghe.theme.outlineLightMediumContrast,
    outlineVariant = com.app.tintuccongnghe.theme.outlineVariantLightMediumContrast,
    scrim = com.app.tintuccongnghe.theme.scrimLightMediumContrast,
    inverseSurface = com.app.tintuccongnghe.theme.inverseSurfaceLightMediumContrast,
    inverseOnSurface = com.app.tintuccongnghe.theme.inverseOnSurfaceLightMediumContrast,
    inversePrimary = com.app.tintuccongnghe.theme.inversePrimaryLightMediumContrast,
    surfaceDim = com.app.tintuccongnghe.theme.surfaceDimLightMediumContrast,
    surfaceBright = com.app.tintuccongnghe.theme.surfaceBrightLightMediumContrast,
    surfaceContainerLowest = com.app.tintuccongnghe.theme.surfaceContainerLowestLightMediumContrast,
    surfaceContainerLow = com.app.tintuccongnghe.theme.surfaceContainerLowLightMediumContrast,
    surfaceContainer = com.app.tintuccongnghe.theme.surfaceContainerLightMediumContrast,
    surfaceContainerHigh = com.app.tintuccongnghe.theme.surfaceContainerHighLightMediumContrast,
    surfaceContainerHighest = com.app.tintuccongnghe.theme.surfaceContainerHighestLightMediumContrast,
)

private val highContrastLightColorScheme = lightColorScheme(
    primary = com.app.tintuccongnghe.theme.primaryLightHighContrast,
    onPrimary = com.app.tintuccongnghe.theme.onPrimaryLightHighContrast,
    primaryContainer = com.app.tintuccongnghe.theme.primaryContainerLightHighContrast,
    onPrimaryContainer = com.app.tintuccongnghe.theme.onPrimaryContainerLightHighContrast,
    secondary = com.app.tintuccongnghe.theme.secondaryLightHighContrast,
    onSecondary = com.app.tintuccongnghe.theme.onSecondaryLightHighContrast,
    secondaryContainer = com.app.tintuccongnghe.theme.secondaryContainerLightHighContrast,
    onSecondaryContainer = com.app.tintuccongnghe.theme.onSecondaryContainerLightHighContrast,
    tertiary = com.app.tintuccongnghe.theme.tertiaryLightHighContrast,
    onTertiary = com.app.tintuccongnghe.theme.onTertiaryLightHighContrast,
    tertiaryContainer = com.app.tintuccongnghe.theme.tertiaryContainerLightHighContrast,
    onTertiaryContainer = com.app.tintuccongnghe.theme.onTertiaryContainerLightHighContrast,
    error = com.app.tintuccongnghe.theme.errorLightHighContrast,
    onError = com.app.tintuccongnghe.theme.onErrorLightHighContrast,
    errorContainer = com.app.tintuccongnghe.theme.errorContainerLightHighContrast,
    onErrorContainer = com.app.tintuccongnghe.theme.onErrorContainerLightHighContrast,
    background = com.app.tintuccongnghe.theme.backgroundLightHighContrast,
    onBackground = com.app.tintuccongnghe.theme.onBackgroundLightHighContrast,
    surface = com.app.tintuccongnghe.theme.surfaceLightHighContrast,
    onSurface = com.app.tintuccongnghe.theme.onSurfaceLightHighContrast,
    surfaceVariant = com.app.tintuccongnghe.theme.surfaceVariantLightHighContrast,
    onSurfaceVariant = com.app.tintuccongnghe.theme.onSurfaceVariantLightHighContrast,
    outline = com.app.tintuccongnghe.theme.outlineLightHighContrast,
    outlineVariant = com.app.tintuccongnghe.theme.outlineVariantLightHighContrast,
    scrim = com.app.tintuccongnghe.theme.scrimLightHighContrast,
    inverseSurface = com.app.tintuccongnghe.theme.inverseSurfaceLightHighContrast,
    inverseOnSurface = com.app.tintuccongnghe.theme.inverseOnSurfaceLightHighContrast,
    inversePrimary = com.app.tintuccongnghe.theme.inversePrimaryLightHighContrast,
    surfaceDim = com.app.tintuccongnghe.theme.surfaceDimLightHighContrast,
    surfaceBright = com.app.tintuccongnghe.theme.surfaceBrightLightHighContrast,
    surfaceContainerLowest = com.app.tintuccongnghe.theme.surfaceContainerLowestLightHighContrast,
    surfaceContainerLow = com.app.tintuccongnghe.theme.surfaceContainerLowLightHighContrast,
    surfaceContainer = com.app.tintuccongnghe.theme.surfaceContainerLightHighContrast,
    surfaceContainerHigh = com.app.tintuccongnghe.theme.surfaceContainerHighLightHighContrast,
    surfaceContainerHighest = com.app.tintuccongnghe.theme.surfaceContainerHighestLightHighContrast,
)

private val mediumContrastDarkColorScheme = darkColorScheme(
    primary = com.app.tintuccongnghe.theme.primaryDarkMediumContrast,
    onPrimary = com.app.tintuccongnghe.theme.onPrimaryDarkMediumContrast,
    primaryContainer = com.app.tintuccongnghe.theme.primaryContainerDarkMediumContrast,
    onPrimaryContainer = com.app.tintuccongnghe.theme.onPrimaryContainerDarkMediumContrast,
    secondary = com.app.tintuccongnghe.theme.secondaryDarkMediumContrast,
    onSecondary = com.app.tintuccongnghe.theme.onSecondaryDarkMediumContrast,
    secondaryContainer = com.app.tintuccongnghe.theme.secondaryContainerDarkMediumContrast,
    onSecondaryContainer = com.app.tintuccongnghe.theme.onSecondaryContainerDarkMediumContrast,
    tertiary = com.app.tintuccongnghe.theme.tertiaryDarkMediumContrast,
    onTertiary = com.app.tintuccongnghe.theme.onTertiaryDarkMediumContrast,
    tertiaryContainer = com.app.tintuccongnghe.theme.tertiaryContainerDarkMediumContrast,
    onTertiaryContainer = com.app.tintuccongnghe.theme.onTertiaryContainerDarkMediumContrast,
    error = com.app.tintuccongnghe.theme.errorDarkMediumContrast,
    onError = com.app.tintuccongnghe.theme.onErrorDarkMediumContrast,
    errorContainer = com.app.tintuccongnghe.theme.errorContainerDarkMediumContrast,
    onErrorContainer = com.app.tintuccongnghe.theme.onErrorContainerDarkMediumContrast,
    background = com.app.tintuccongnghe.theme.backgroundDarkMediumContrast,
    onBackground = com.app.tintuccongnghe.theme.onBackgroundDarkMediumContrast,
    surface = com.app.tintuccongnghe.theme.surfaceDarkMediumContrast,
    onSurface = com.app.tintuccongnghe.theme.onSurfaceDarkMediumContrast,
    surfaceVariant = com.app.tintuccongnghe.theme.surfaceVariantDarkMediumContrast,
    onSurfaceVariant = com.app.tintuccongnghe.theme.onSurfaceVariantDarkMediumContrast,
    outline = com.app.tintuccongnghe.theme.outlineDarkMediumContrast,
    outlineVariant = com.app.tintuccongnghe.theme.outlineVariantDarkMediumContrast,
    scrim = com.app.tintuccongnghe.theme.scrimDarkMediumContrast,
    inverseSurface = com.app.tintuccongnghe.theme.inverseSurfaceDarkMediumContrast,
    inverseOnSurface = com.app.tintuccongnghe.theme.inverseOnSurfaceDarkMediumContrast,
    inversePrimary = com.app.tintuccongnghe.theme.inversePrimaryDarkMediumContrast,
    surfaceDim = com.app.tintuccongnghe.theme.surfaceDimDarkMediumContrast,
    surfaceBright = com.app.tintuccongnghe.theme.surfaceBrightDarkMediumContrast,
    surfaceContainerLowest = com.app.tintuccongnghe.theme.surfaceContainerLowestDarkMediumContrast,
    surfaceContainerLow = com.app.tintuccongnghe.theme.surfaceContainerLowDarkMediumContrast,
    surfaceContainer = com.app.tintuccongnghe.theme.surfaceContainerDarkMediumContrast,
    surfaceContainerHigh = com.app.tintuccongnghe.theme.surfaceContainerHighDarkMediumContrast,
    surfaceContainerHighest = com.app.tintuccongnghe.theme.surfaceContainerHighestDarkMediumContrast,
)

private val highContrastDarkColorScheme = darkColorScheme(
    primary = com.app.tintuccongnghe.theme.primaryDarkHighContrast,
    onPrimary = com.app.tintuccongnghe.theme.onPrimaryDarkHighContrast,
    primaryContainer = com.app.tintuccongnghe.theme.primaryContainerDarkHighContrast,
    onPrimaryContainer = com.app.tintuccongnghe.theme.onPrimaryContainerDarkHighContrast,
    secondary = com.app.tintuccongnghe.theme.secondaryDarkHighContrast,
    onSecondary = com.app.tintuccongnghe.theme.onSecondaryDarkHighContrast,
    secondaryContainer = com.app.tintuccongnghe.theme.secondaryContainerDarkHighContrast,
    onSecondaryContainer = com.app.tintuccongnghe.theme.onSecondaryContainerDarkHighContrast,
    tertiary = com.app.tintuccongnghe.theme.tertiaryDarkHighContrast,
    onTertiary = com.app.tintuccongnghe.theme.onTertiaryDarkHighContrast,
    tertiaryContainer = com.app.tintuccongnghe.theme.tertiaryContainerDarkHighContrast,
    onTertiaryContainer = com.app.tintuccongnghe.theme.onTertiaryContainerDarkHighContrast,
    error = com.app.tintuccongnghe.theme.errorDarkHighContrast,
    onError = com.app.tintuccongnghe.theme.onErrorDarkHighContrast,
    errorContainer = com.app.tintuccongnghe.theme.errorContainerDarkHighContrast,
    onErrorContainer = com.app.tintuccongnghe.theme.onErrorContainerDarkHighContrast,
    background = com.app.tintuccongnghe.theme.backgroundDarkHighContrast,
    onBackground = com.app.tintuccongnghe.theme.onBackgroundDarkHighContrast,
    surface = com.app.tintuccongnghe.theme.surfaceDarkHighContrast,
    onSurface = com.app.tintuccongnghe.theme.onSurfaceDarkHighContrast,
    surfaceVariant = com.app.tintuccongnghe.theme.surfaceVariantDarkHighContrast,
    onSurfaceVariant = com.app.tintuccongnghe.theme.onSurfaceVariantDarkHighContrast,
    outline = com.app.tintuccongnghe.theme.outlineDarkHighContrast,
    outlineVariant = com.app.tintuccongnghe.theme.outlineVariantDarkHighContrast,
    scrim = com.app.tintuccongnghe.theme.scrimDarkHighContrast,
    inverseSurface = com.app.tintuccongnghe.theme.inverseSurfaceDarkHighContrast,
    inverseOnSurface = com.app.tintuccongnghe.theme.inverseOnSurfaceDarkHighContrast,
    inversePrimary = com.app.tintuccongnghe.theme.inversePrimaryDarkHighContrast,
    surfaceDim = com.app.tintuccongnghe.theme.surfaceDimDarkHighContrast,
    surfaceBright = com.app.tintuccongnghe.theme.surfaceBrightDarkHighContrast,
    surfaceContainerLowest = com.app.tintuccongnghe.theme.surfaceContainerLowestDarkHighContrast,
    surfaceContainerLow = com.app.tintuccongnghe.theme.surfaceContainerLowDarkHighContrast,
    surfaceContainer = com.app.tintuccongnghe.theme.surfaceContainerDarkHighContrast,
    surfaceContainerHigh = com.app.tintuccongnghe.theme.surfaceContainerHighDarkHighContrast,
    surfaceContainerHighest = com.app.tintuccongnghe.theme.surfaceContainerHighestDarkHighContrast,
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
    dynamicColor: Boolean = true,
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

    MaterialExpressiveTheme(
        colorScheme = replyColorScheme,
        motionScheme = MotionScheme.expressive(),
        typography = replyTypography.scaled(fontScale),
        shapes = com.app.tintuccongnghe.theme.shapes,
        content = content
    )
}
