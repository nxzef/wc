package com.nxzef.wc.shared.model

import kotlinx.serialization.Serializable

@Serializable
enum class LeadSource {
    INSTAGRAM, FACEBOOK, GOOGLE,
    REFERRAL, WALK_IN, OTHER
}

@Serializable
enum class EventType {
    WEDDING, PORTRAIT, CORPORATE, EVENT
}

@Serializable
data class Lead(
    val id: String,
    val fullName: String,
    val phone: String,
    val email: String? = null,
    val source: LeadSource,
    val eventType: EventType,
    val eventDate: String? = null,
    val location: String? = null,
    val statusName: String,
    val customStatus: LeadStatus? = null,
    val priority: Int = 0,
    val lostReason: String? = null,
    val notes: String? = null,
    val addedBy: String,
    val assignedTo: String,
    val createdAt: String,
    val budget: Double = 0.0
)

@Serializable
data class CreateLeadRequest(
    val fullName: String,
    val phone: String,
    val email: String? = null,
    val source: LeadSource,
    val eventType: EventType,
    val eventDate: String? = null,
    val location: String? = null,
    val priority: Int = 0,
    val notes: String? = null,
    val assignedTo: String,
    val budget: Double = 0.0
)

@Serializable
data class UpdateLeadStatusRequest(
    val customStatusId: String,
    val lostReason: String? = null,
    val notes: String? = null
)
