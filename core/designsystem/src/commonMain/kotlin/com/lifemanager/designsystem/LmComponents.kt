package com.lifemanager.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

object LmSpacing {
    val XSmall = 4.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val XLarge = 24.dp
    val XXLarge = 32.dp
}

enum class LmStatTone {
    Neutral,
    Positive,
    Warning,
    Critical,
}

@Composable
fun LmCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(LmSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(LmSpacing.Medium),
            content = content,
        )
    }
}

@Composable
fun LmSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    icon: ImageVector? = null,
    action: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LmSpacing.Small),
    ) {
        when {
            icon != null -> Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            emoji != null -> Text(text = emoji, style = MaterialTheme.typography.titleMedium)
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (action != null) {
            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                action()
            }
        }
    }
}

@Composable
fun LmListItem(
    headline: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    enabled: Boolean = true,
    showDivider: Boolean = false,
    onClick: (() -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val itemModifier = modifier
        .fillMaxWidth()
        .then(if (enabled && onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(vertical = LmSpacing.Medium)

    Column {
        Row(
            modifier = itemModifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LmSpacing.Medium),
        ) {
            if (leadingContent != null) {
                leadingContent()
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (supporting != null) {
                    Text(
                        text = supporting,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
        if (showDivider) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
fun LmEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    LmFeedbackState(
        title = title,
        message = message,
        modifier = modifier,
        emoji = emoji,
        actionLabel = actionLabel,
        onAction = onAction,
        actionIsPrimary = true,
    )
}

@Composable
fun LmErrorState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    LmFeedbackState(
        title = title,
        message = message,
        modifier = modifier,
        emoji = "!",
        actionLabel = actionLabel,
        onAction = onAction,
        actionIsPrimary = false,
        accentColor = MaterialTheme.colorScheme.errorContainer,
    )
}

@Composable
fun LmLoadingState(
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(LmSpacing.XLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LmSpacing.Medium),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
            strokeWidth = 3.dp,
        )
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun LmStatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    tone: LmStatTone = LmStatTone.Neutral,
) {
    val accent = when (tone) {
        LmStatTone.Neutral -> MaterialTheme.colorScheme.primaryContainer
        LmStatTone.Positive -> MaterialTheme.colorScheme.secondaryContainer
        LmStatTone.Warning -> MaterialTheme.colorScheme.tertiaryContainer
        LmStatTone.Critical -> MaterialTheme.colorScheme.errorContainer
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(LmSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(LmSpacing.Small),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(accent, CircleShape),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (supporting != null) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun LmTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .defaultMinSize(minHeight = 64.dp)
                    .padding(horizontal = LmSpacing.Large, vertical = LmSpacing.Small),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LmSpacing.Medium),
            ) {
                if (navigationIcon != null) {
                    navigationIcon()
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(LmSpacing.XSmall),
                ) {
                    actions()
                }
            }
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun LmFeedbackState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    actionIsPrimary: Boolean,
    accentColor: Color = MaterialTheme.colorScheme.primaryContainer,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(LmSpacing.XLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LmSpacing.Medium),
    ) {
        if (emoji != null) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accentColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emoji, style = MaterialTheme.typography.titleLarge)
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (actionLabel != null && onAction != null) {
            if (actionIsPrimary) {
                Button(onClick = onAction) {
                    Text(actionLabel)
                }
            } else {
                TextButton(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}
