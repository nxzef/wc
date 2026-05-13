package com.nxzef.wc.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    data object Welcome : Route
    @Serializable
    data object Register : Route
    @Serializable
    data object JoinTeam : Route
    @Serializable
    data object ForgotPassword : Route
    @Serializable
    data object MemberLogin : Route
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
    @Serializable data class Quotes(val leadId: String, val clientName: String, val clientEmail: String) : Route
    @Serializable data object Tasks : Route
    @Serializable data class ProjectExpenses(val bookingId: String) : Route
    @Serializable data object BookingCalendar : Route
    @Serializable data class Project(val bookingId: String) : Route
    @Serializable data object Analytics : Route
}
