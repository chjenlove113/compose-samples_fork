/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.reply.wear.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.MotionScheme
import androidx.wear.compose.material3.dynamicColorScheme

private val ReplyColorScheme = ColorScheme(
    primary = Color(0xFFB5C4FF),
    primaryDim = Color(0xFF91A7FF),
    primaryContainer = Color(0xFF263F77),
    onPrimary = Color(0xFF002D6B),
    onPrimaryContainer = Color(0xFFDCE2FF),
    secondary = Color(0xFFFFB1C8),
    secondaryDim = Color(0xFFE591AB),
    secondaryContainer = Color(0xFF653044),
    onSecondary = Color(0xFF541D34),
    onSecondaryContainer = Color(0xFFFFD9E2),
    tertiary = Color(0xFF82D3E9),
    tertiaryDim = Color(0xFF62B7CC),
    tertiaryContainer = Color(0xFF174E5B),
    onTertiary = Color(0xFF003640),
    onTertiaryContainer = Color(0xFFB4EBFA),
    surfaceContainerLow = Color(0xFF151B27),
    surfaceContainer = Color(0xFF1B2230),
    surfaceContainerHigh = Color(0xFF252D3B),
    onSurface = Color(0xFFE3E7F3),
    onSurfaceVariant = Color(0xFFC3C6D4),
    outline = Color(0xFF8D909E),
    outlineVariant = Color(0xFF434752),
    background = Color(0xFF090E17),
    onBackground = Color(0xFFE3E7F3),
)

@Composable
fun ReplyWearTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colorScheme = remember(context) {
        dynamicColorScheme(context) ?: ReplyColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}
