package com.nxzef.wc.shared.dto

import com.nxzef.wc.shared.model.Notification
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val bookingId: String? = null,
    val taskId: String? = null,
    val createdAt: String
)

fun NotificationDto.toDomain(): Notification {
    return Notification(
        id = id,
        userId = userId,
        title = title,
        message = message,
        isRead = isRead,
        bookingId = bookingId,
        taskId = taskId,
        createdAt = createdAt
    )
}

fun Notification.toDto(): NotificationDto {
    return NotificationDto(
        id = id,
        userId = userId,
        title = title,
        message = message,
        isRead = isRead,
        bookingId = bookingId,
        taskId = taskId,
        createdAt = createdAt
    )
}
