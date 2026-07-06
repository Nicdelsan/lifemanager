package com.lifemanager.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

private enum class CatalogSection(val label: String) {
    Theme("Theme"),
    Cards("Cards"),
    Lists("Lists"),
    States("States"),
    TopBar("Top bar"),
}

@Composable
fun LmComponentCatalogScreen(modifier: Modifier = Modifier) {
    var selectedSection by remember { mutableStateOf(CatalogSection.Theme) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(LmSpacing.Large),
    ) {
        LmTopBar(title = "LifeManager UI", subtitle = selectedSection.label)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LmSpacing.Small),
        ) {
            items(CatalogSection.entries.toList()) { section ->
                AssistChip(
                    onClick = { selectedSection = section },
                    label = { Text(section.label) },
                )
            }
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(LmSpacing.Large),
        ) {
            items(listOf(selectedSection)) { section ->
                when (section) {
                    CatalogSection.Theme -> CatalogThemeSection()
                    CatalogSection.Cards -> CatalogCardsSection()
                    CatalogSection.Lists -> CatalogListsSection()
                    CatalogSection.States -> CatalogStatesSection()
                    CatalogSection.TopBar -> CatalogTopBarSection()
                }
            }
        }
    }
}

@Composable
private fun CatalogThemeSection() {
    Column(verticalArrangement = Arrangement.spacedBy(LmSpacing.Medium)) {
        LmSectionHeader(title = "Typography", emoji = "T")
        Text("Display", style = MaterialTheme.typography.headlineLarge)
        Text("Section title", style = MaterialTheme.typography.titleLarge)
        Text("Body copy for dense operational screens.", style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(LmSpacing.Medium)) {
            LmStatTile(
                value = "82%",
                label = "Primary",
                supporting = "Neutral tile",
                modifier = Modifier.weight(1f),
            )
            LmStatTile(
                value = "+12",
                label = "Positive",
                supporting = "Accent tile",
                tone = LmStatTone.Positive,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CatalogCardsSection() {
    LmCard {
        LmSectionHeader(
            title = "Monthly summary",
            emoji = "$",
            action = { TextButton(onClick = {}) { Text("Open") } },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(LmSpacing.Medium)) {
            LmStatTile(value = "1.250", label = "Income", modifier = Modifier.weight(1f))
            LmStatTile(value = "890", label = "Costs", tone = LmStatTone.Warning, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun CatalogListsSection() {
    LmCard {
        LmSectionHeader(title = "List items", emoji = "#")
        LmListItem(
            headline = "Primary item",
            supporting = "Secondary information with two-line support.",
            trailingContent = { Text("12:30", style = MaterialTheme.typography.labelMedium) },
            showDivider = true,
        )
        LmListItem(
            headline = "Disabled item",
            supporting = "Muted state for unavailable actions.",
            enabled = false,
        )
    }
}

@Composable
private fun CatalogStatesSection() {
    Column(verticalArrangement = Arrangement.spacedBy(LmSpacing.Medium)) {
        LmEmptyState(
            title = "No data",
            message = "Content modules use this state before the first entry.",
            emoji = "+",
            actionLabel = "Create",
            onAction = {},
        )
        LmErrorState(
            title = "Something failed",
            message = "Recoverable errors keep the screen stable.",
            actionLabel = "Retry",
            onAction = {},
        )
        LmLoadingState(label = "Loading")
    }
}

@Composable
private fun CatalogTopBarSection() {
    Column(verticalArrangement = Arrangement.spacedBy(LmSpacing.Medium)) {
        LmTopBar(title = "Dashboard", subtitle = "Today")
        LmTopBar(title = "Finance", actions = { TextButton(onClick = {}) { Text("Edit") } })
    }
}

@Preview
@Composable
private fun LmCardPreview() {
    LifeManagerTheme {
        CatalogCardsSection()
    }
}

@Preview
@Composable
private fun LmSectionHeaderPreview() {
    LifeManagerTheme {
        LmSectionHeader(title = "Section", emoji = "*")
    }
}

@Preview
@Composable
private fun LmListItemPreview() {
    LifeManagerTheme {
        LmCard {
            LmListItem(headline = "Item", supporting = "Supporting text")
        }
    }
}

@Preview
@Composable
private fun LmEmptyStatePreview() {
    LifeManagerTheme {
        LmEmptyState(title = "No entries", message = "Add the first record.", emoji = "+")
    }
}

@Preview
@Composable
private fun LmErrorStatePreview() {
    LifeManagerTheme {
        LmErrorState(title = "Error", message = "Try again.", actionLabel = "Retry", onAction = {})
    }
}

@Preview
@Composable
private fun LmLoadingStatePreview() {
    LifeManagerTheme {
        LmLoadingState(label = "Loading")
    }
}

@Preview
@Composable
private fun LmStatTilePreview() {
    LifeManagerTheme {
        LmStatTile(value = "64%", label = "Progress", supporting = "On track")
    }
}

@Preview
@Composable
private fun LmTopBarPreview() {
    LifeManagerTheme {
        LmTopBar(title = "LifeManager", subtitle = "Dashboard")
    }
}

@Preview
@Composable
private fun LmComponentCatalogPreview() {
    LifeManagerTheme {
        LmComponentCatalogScreen()
    }
}

@Preview
@Composable
private fun LmComponentCatalogDarkPreview() {
    LifeManagerTheme(darkTheme = true) {
        LmComponentCatalogScreen()
    }
}
