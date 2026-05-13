package com.nxzef.wc.shared.dto

import com.nxzef.wc.shared.model.EventType
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.LeadSource
import com.nxzef.wc.shared.model.LeadStatus
import kotlinx.serialization.Serializable

@Serializable
data class LeadDto(
    val id: String,
    val fullName: String,
    val phone: String,
    val email: String? = null,
    val source: String,
    val eventType: String,
    val eventDate: String? = null,
    val eventEndDate: String? = null,
    val location: String? = null,
    val statusName: String,
    val customStatusId: String? = null,
    val customStatusColor: String? = null,
    val priority: Int = 0,
    val lostReason: String? = null,
    val notes: String? = null,
    val addedBy: String,
    val assignedTo: String,
    val createdAt: String,
    val budget: Double = 0.0,
    val isWon: Boolean = false,
    val isLost: Boolean = false
)

fun LeadDto.toDomain(): Lead {
    val customStatus = if (customStatusId != null) {
        LeadStatus(
            id = customStatusId,
            name = statusName,
            color = customStatusColor ?: "#2196F3",
            isDefault = false
        )
    } else null

    return Lead(
        id = id,
        fullName = fullName,
        phone = phone,
        email = email,
        source = try { LeadSource.valueOf(source) } catch (e: Exception) { LeadSource.OTHER },
        eventType = try { EventType.valueOf(eventType) } catch (e: Exception) { EventType.EVENT },
        eventDate = eventDate,
        eventEndDate = eventEndDate,
        location = location,
        statusName = statusName,
        customStatus = customStatus,
        priority = priority,
        lostReason = lostReason,
        notes = notes,
        addedBy = addedBy,
        assignedTo = assignedTo,
        createdAt = createdAt,
        budget = budget,
        isWon = isWon,
        isLost = isLost
    )
}

fun Lead.toDto(): LeadDto {
    return LeadDto(
        id = id,
        fullName = fullName,
        phone = phone,
        email = email,
        source = source.name,
        eventType = eventType.name,
        eventDate = eventDate,
        eventEndDate = eventEndDate,
        location = location,
        statusName = statusName,
        customStatusId = customStatus?.id,
        customStatusColor = customStatus?.color,
        priority = priority,
        lostReason = lostReason,
        notes = notes,
        addedBy = addedBy,
        assignedTo = assignedTo,
        createdAt = createdAt,
        budget = budget,
        isWon = isWon,
        isLost = isLost
    )
}
