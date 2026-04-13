package com.nxzef.wc.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DashboardStats(
    val totalLeadsThisMonth: Int,
    val totalBookingsThisMonth: Int,
    val totalRevenueThisMonth: Double,
    val pendingPayments: Double,
    val openLeads: Int,
    val pendingDeliveries: Int,
    val leadsBySource: Map<String, Int>,
    val recentLeads: List<Lead>,
    val upcomingBookings: List<Booking>
)

@Serializable
data class Lead(
    val id: String,
    val fullName: String,
    val phone: String,
    val email: String? = null,
    val source: String,
    val eventType: String,
    val eventDate: String? = null,
    val location: String? = null,
    val status: String,
    val lostReason: String? = null,
    val notes: String? = null,
    val addedBy: String,
    val assignedTo: String,
    val createdAt: String
)

@Serializable
data class Booking(
    val id: String,
    val leadId: String,
    val quoteId: String,
    val eventDate: String,
    val eventType: String,
    val location: String,
    val status: String,
    val notes: String? = null,
    val createdAt: String
)