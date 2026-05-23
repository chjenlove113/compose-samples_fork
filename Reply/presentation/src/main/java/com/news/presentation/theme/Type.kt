package com.news.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp


// Material 3 typography
val replyTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

fun Typography.scaled(scale: Float): Typography {
    return Typography(
        displayLarge = displayLarge.scaled(scale),
        displayMedium = displayMedium.scaled(scale),
        displaySmall = displaySmall.scaled(scale),
        headlineLarge = headlineLarge.scaled(scale),
        headlineMedium = headlineMedium.scaled(scale),
        headlineSmall = headlineSmall.scaled(scale),
        titleLarge = titleLarge.scaled(scale),
        titleMedium = titleMedium.scaled(scale),
        titleSmall = titleSmall.scaled(scale),
        bodyLarge = bodyLarge.scaled(scale),
        bodyMedium = bodyMedium.scaled(scale),
        bodySmall = bodySmall.scaled(scale),
        labelLarge = labelLarge.scaled(scale),
        labelMedium = labelMedium.scaled(scale),
        labelSmall = labelSmall.scaled(scale)
    )
}

fun TextStyle.scaled(scale: Float): TextStyle {
    return this.copy(
        fontSize = fontSize.scaled(scale),
        lineHeight = lineHeight.scaled(scale),
        letterSpacing = letterSpacing.scaled(scale)
    )
}

fun androidx.compose.ui.unit.TextUnit.scaled(scale: Float): androidx.compose.ui.unit.TextUnit {
    return if (isUnspecified) this else this * scale
}
