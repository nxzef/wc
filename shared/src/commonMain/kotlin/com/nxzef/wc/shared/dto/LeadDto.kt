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
    val location: String? = null,
    val status: String,
    val lostReason: String? = null,
    val notes: String? = null,
    val addedBy: String,
    val assignedTo: String,
    val createdAt: String
)

fun LeadDto.toDomain(): Lead {
    return Lead(
        id = id,
        fullName = fullName,
        phone = phone,
        email = email,
        source = try { LeadSource.valueOf(source) } catch (e: Exception) { LeadSource.OTHER },
        eventType = try { EventType.valueOf(eventType) } catch (e: Exception) { EventType.EVENT },
        eventDate = eventDate,
        location = location,
        status = try { LeadStatus.valueOf(status) } catch (e: Exception) { LeadStatus.NEW },
        lostReason = lostReason,
        notes = notes,
        addedBy = addedBy,
        assignedTo = assignedTo,
        createdAt = createdAt
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
        location = location,
        status = status.name,
        lostReason = lostReason,
        notes = notes,
        addedBy = addedBy,
        assignedTo = assignedTo,
        createdAt = createdAt
    )
}