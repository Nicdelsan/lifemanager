package com.lifemanager.app

import com.lifemanager.dashboardapi.FeatureModule

/**
 * Static registry of active feature modules. WP-0.1 ships it empty; each
 * feature WP registers itself here as its only allowed change outside its
 * own module (see docs/current-wp.md).
 */
object FeatureModuleRegistry {
    val modules: List<FeatureModule> = listOf()
}
