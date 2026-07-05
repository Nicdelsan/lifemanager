package com.lifemanager.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import org.junit.jupiter.api.Test

/**
 * Enforces the module dependency rules from implementation-plan.md §3.1:
 * - feature:* may depend on core:*, never on another feature
 * - core:* never depends on feature:*
 * - :app is the only module allowed to depend on everything
 */
class ArchitectureTest {

    @Test
    fun `module dependencies follow the layering rules from the implementation plan`() {
        Konsist
            .scopeFromProject()
            .assertArchitecture {
                val dashboardApi = Layer("DashboardApi", "com.lifemanager.dashboardapi..")
                val common = Layer("Common", "com.lifemanager.common..")
                val designSystem = Layer("DesignSystem", "com.lifemanager.designsystem..")
                val database = Layer("Database", "com.lifemanager.database..")
                val sync = Layer("Sync", "com.lifemanager.sync..")
                val finance = Layer("Finance", "com.lifemanager.finance..")
                val diet = Layer("Diet", "com.lifemanager.diet..")
                val work = Layer("Work", "com.lifemanager.work..")
                val wearable = Layer("Wearable", "com.lifemanager.wearable..")
                val app = Layer("App", "com.lifemanager.app..")

                dashboardApi.dependsOnNothing()
                common.dependsOnNothing()
                designSystem.dependsOnNothing()
                database.dependsOnNothing()
                sync.dependsOnNothing()

                finance.dependsOn(dashboardApi, common, designSystem, database)
                diet.dependsOn(dashboardApi, common, designSystem, database)
                work.dependsOn(dashboardApi, common, designSystem, database)
                wearable.dependsOn(dashboardApi, common, designSystem, database)

                app.dependsOn(
                    dashboardApi,
                    common,
                    designSystem,
                    database,
                    sync,
                    finance,
                    diet,
                    work,
                    wearable,
                )
            }
    }
}
