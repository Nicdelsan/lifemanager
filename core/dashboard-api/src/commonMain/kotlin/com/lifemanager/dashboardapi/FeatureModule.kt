package com.lifemanager.dashboardapi

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import org.jetbrains.compose.resources.StringResource
import org.koin.core.module.Module

interface FeatureModule {
    val id: String
    val displayName: StringResource
    val icon: ImageVector
    val koinModule: Module

    fun dashboardCards(): List<DashboardCardProvider>
    fun navGraph(builder: NavGraphBuilder, navController: NavController)
}
