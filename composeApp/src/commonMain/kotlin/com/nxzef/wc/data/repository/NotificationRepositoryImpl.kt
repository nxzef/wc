package com.nxzef.wc.data.repository

import com.nxzef.wc.data.remote.NotificationService
import com.nxzef.wc.domain.repository.NotificationRepository

class NotificationRepositoryImpl(
    private val service: NotificationService
) : NotificationRepository {

    override suspend fun getMyNotifications() =
        runCatching { service.getMyNotifications() }

    override suspend fun getUnreadCount() =
        runCatching { service.getUnreadCount() }

    override suspend fun markAsRead(id: String) =
        runCatching { service.markAsRead(id) }

    override suspend fun markAllAsRead() =
        runCatching { service.markAllAsRead() }
}