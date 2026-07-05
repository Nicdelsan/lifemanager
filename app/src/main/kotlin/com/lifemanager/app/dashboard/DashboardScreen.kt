package com.lifemanager.app.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.lifemanager.app.FeatureModuleRegistry
import com.lifemanager.app.R

const val DASHBOARD_ROUTE = "dashboard"

fun NavGraphBuilder.dashboardGraph() {
    composable(DASHBOARD_ROUTE) {
        DashboardScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(modifier: Modifier = Modifier) {
    val viewModel: DashboardViewModel = viewModel(
        factory = viewModelFactory {
            initializer { DashboardViewModel(FeatureModuleRegistry.modules) }
        },
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.dashboard_title)) }) },
    ) { paddingValues ->
        when (uiState) {
            is DashboardUiState.Empty -> DashboardEmptyContent(modifier = Modifier.padding(paddingValues))
            is DashboardUiState.Content -> {
                // Card rendering arrives with the first registered feature module (WP-2.1).
            }
        }
    }
}

@Composable
private fun DashboardEmptyContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.dashboard_empty_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.dashboard_empty_description),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
