package com.nxzef.wc.data.repository

import com.nxzef.wc.data.remote.NotificationService
import com.nxzef.wc.domain.repository.NotificationRepository
import com.nxzef.wc.shared.dto.toDomain
import com.nxzef.wc.shared.util.AppResult

class NotificationRepositoryImpl(
    private val service: NotificationService
) : NotificationRepository {

    override suspend fun getMyNotifications() = try {
        AppResult.Success(service.getMyNotifications().map { it.toDomain() })
    } catch (e: Exception) {
        AppResult.Failure(e)
    }

    override suspend fun getUnreadCount() = try {
        AppResult.Success(service.getUnreadCount())
    } catch (e: Exception) {
        AppResult.Failure(e)
    }

    override suspend fun markAsRead(id: String) = try {
        AppResult.Success(service.markAsRead(id))
    } catch (e: Exception) {
        AppResult.Failure(e)
    }

    override suspend fun markAllAsRead() = try {
        AppResult.Success(service.markAllAsRead())
    } catch (e: Exception) {
        AppResult.Failure(e)
    }
}