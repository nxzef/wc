package com.nxzef.wc.domain.repository

import com.nxzef.wc.shared.model.Notification
import com.nxzef.wc.shared.util.AppResult

interface NotificationRepository {
    suspend fun getMyNotifications(): AppResult<List<Notification>>
    suspend fun getUnreadCount(): AppResult<Int>
    suspend fun markAsRead(id: String): AppResult<Boolean>
    suspend fun markAllAsRead(): AppResult<Int>
}