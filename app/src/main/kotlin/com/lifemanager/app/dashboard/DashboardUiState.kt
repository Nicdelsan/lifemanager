package com.lifemanager.app.dashboard

import com.lifemanager.dashboardapi.DashboardCardProvider

sealed interface DashboardUiState {
    data object Empty : DashboardUiState
    data class Content(val cards: List<DashboardCardProvider>) : DashboardUiState
}
