package com.lifemanager.app.dashboard

import androidx.lifecycle.ViewModel
import com.lifemanager.dashboardapi.FeatureModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DashboardViewModel(featureModules: List<FeatureModule>) : ViewModel() {
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Empty)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        val cards = featureModules.flatMap { it.dashboardCards() }
        _uiState.value = if (cards.isEmpty()) DashboardUiState.Empty else DashboardUiState.Content(cards)
    }
}
