package com.lifemanager.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.lifemanager.app.dashboard.DASHBOARD_ROUTE
import com.lifemanager.app.dashboard.dashboardGraph
import com.lifemanager.designsystem.LifeManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LifeManagerTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = DASHBOARD_ROUTE,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    dashboardGraph()
                    designSystemCatalogDebugGraph()
                    FeatureModuleRegistry.modules.forEach { module ->
                        module.navGraph(this, navController)
                    }
                }
            }
        }
    }
}
