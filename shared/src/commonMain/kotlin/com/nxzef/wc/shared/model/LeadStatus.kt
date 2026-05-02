package com.nxzef.wc.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class LeadStatus(
    val id: String,
    val name: String,
    val color: String,
    val isDefault: Boolean
)

@Serializable
data class CreateLeadStatusRequest(
    val name: String,
    val color: String
)
