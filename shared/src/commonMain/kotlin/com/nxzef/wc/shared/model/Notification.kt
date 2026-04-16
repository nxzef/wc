package com.nxzef.wc.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val bookingId: String? = null,
    val taskId: String? = null,
    val createdAt: String
)

@Serializable
data class CreateNotificationRequest(
    val userId: String,
    val title: String,
    val message: String,
    val bookingId: String? = null,
    val taskId: String? = null
)

@Serializable
data class MarkReadRequest(
    val notificationId: String
)