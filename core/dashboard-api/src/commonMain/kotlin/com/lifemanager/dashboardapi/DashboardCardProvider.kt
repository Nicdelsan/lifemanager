package com.lifemanager.dashboardapi

import androidx.compose.runtime.Composable

interface DashboardCardProvider {
    val cardId: String
    val defaultSize: CardSize

    @Composable
    fun Card(onNavigate: (route: String) -> Unit)
}
