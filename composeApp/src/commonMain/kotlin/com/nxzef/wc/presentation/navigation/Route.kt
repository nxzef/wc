package com.nxzef.wc.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for the WC application.
 */
@Serializable
sealed interface Route {

    @Serializable
    data object Login : Route

    @Serializable
    data object OwnerDashboard : Route

    @Serializable
    data object LeadPipeline : Route

    @Serializable
    data object Marketing : Route

    @Serializable
    data object MyShoots : Route

    @Serializable
    data object EditingQueue : Route
}