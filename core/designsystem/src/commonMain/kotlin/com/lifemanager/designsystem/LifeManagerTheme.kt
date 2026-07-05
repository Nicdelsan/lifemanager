package com.lifemanager.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Placeholder theme for WP-0.1. The real Notion-style palette, typography and
 * shapes are delivered by WP-1.1 (:core:designsystem).
 */
@Composable
fun LifeManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
