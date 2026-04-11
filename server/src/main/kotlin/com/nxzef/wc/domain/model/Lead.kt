package com.nxzef.wc.domain.model

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
enum class LeadStatus {
    NEW, CONTACTED, NEGOTIATING, WON, LOST
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
    val status: LeadStatus,
    val lostReason: String? = null,
    val notes: String? = null,
    val addedBy: String,
    val assignedTo: String,
    val createdAt: String
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
    val notes: String? = null,
    val assignedTo: String
)

@Serializable
data class UpdateLeadStatusRequest(
    val status: LeadStatus,
    val lostReason: String? = null,
    val notes: String? = null
)