package com.nxzef.wc.shared.dto

import com.nxzef.wc.shared.model.Task
import kotlinx.serialization.Serializable

@Serializable
data class TaskDto(
    val id: String,
    val leadId: String? = null,
    val bookingId: String? = null,
    val title: String,
    val description: String? = null,
    val assignedTo: String,
    val dueDate: String? = null,
    val isDone: Boolean,
    val doneAt: String? = null,
    val createdBy: String,
    val createdAt: String
)

fun TaskDto.toDomain(): Task {
    return Task(
        id = id,
        leadId = leadId,
        bookingId = bookingId,
        title = title,
        description = description,
        assignedTo = assignedTo,
        dueDate = dueDate,
        isDone = isDone,
        doneAt = doneAt,
        createdBy = createdBy,
        createdAt = createdAt
    )
}

fun Task.toDto(): TaskDto {
    return TaskDto(
        id = id,
        leadId = leadId,
        bookingId = bookingId,
        title = title,
        description = description,
        assignedTo = assignedTo,
        dueDate = dueDate,
        isDone = isDone,
        doneAt = doneAt,
        createdBy = createdBy,
        createdAt = createdAt
    )
}
