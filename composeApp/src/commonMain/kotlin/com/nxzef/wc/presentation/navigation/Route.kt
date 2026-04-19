package com.nxzef.wc.presentation.navigation

import kotlinx.serialization.Serializable

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
    @Serializable
    data object TeamManagement : Route
    @Serializable
    data object Invoices : Route
    @Serializable data object Settings : Route
    @Serializable data object AddLead : Route
    @Serializable data object Bookings : Route
}