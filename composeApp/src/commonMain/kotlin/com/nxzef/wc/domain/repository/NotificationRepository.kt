package com.nxzef.wc.domain.repository

import com.nxzef.wc.shared.model.Notification

interface NotificationRepository {
    suspend fun getMyNotifications(): Result<List<Notification>>
    suspend fun getUnreadCount(): Result<Int>
    suspend fun markAsRead(id: String): Result<Boolean>
    suspend fun markAllAsRead(): Result<Int>
}