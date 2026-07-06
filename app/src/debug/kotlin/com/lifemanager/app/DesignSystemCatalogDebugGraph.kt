package com.lifemanager.app

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.lifemanager.designsystem.LmComponentCatalogScreen

const val DESIGN_SYSTEM_CATALOG_ROUTE = "debug/design-system-catalog"
const val DESIGN_SYSTEM_CATALOG_DEEP_LINK = "lifemanager://debug/design-system-catalog"

fun NavGraphBuilder.designSystemCatalogDebugGraph() {
    composable(
        route = DESIGN_SYSTEM_CATALOG_ROUTE,
        deepLinks = listOf(navDeepLink { uriPattern = DESIGN_SYSTEM_CATALOG_DEEP_LINK }),
    ) {
        LmComponentCatalogScreen()
    }
}
