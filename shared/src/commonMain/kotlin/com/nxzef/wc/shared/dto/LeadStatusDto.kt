package com.nxzef.wc.shared.dto

import com.nxzef.wc.shared.model.LeadStatus
import kotlinx.serialization.Serializable

@Serializable
data class LeadStatusDto(
    val id: String,
    val name: String,
    val color: String,
    val isDefault: Boolean
)

fun LeadStatusDto.toDomain(): LeadStatus = LeadStatus(
    id = id,
    name = name,
    color = color,
    isDefault = isDefault
)

fun LeadStatus.toDto(): LeadStatusDto = LeadStatusDto(
    id = id,
    name = name,
    color = color,
    isDefault = isDefault
)
