package com.nxzef.wc.domain.service

import com.nxzef.wc.data.repository.NotificationRepository
import com.nxzef.wc.data.repository.UserRepository
import com.nxzef.wc.shared.model.CreateNotificationRequest
import com.nxzef.wc.shared.model.UserRole

class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository
) {
    fun notify(
        userId: String,
        title: String,
        message: String,
        teamId: String,
        bookingId: String? = null,
        taskId: String? = null
    ) {
        notificationRepository.create(
            CreateNotificationRequest(
                userId = userId,
                title = title,
                message = message,
                bookingId = bookingId,
                taskId = taskId
            ),
            teamId = teamId
        )
    }

    fun getOwnerId(teamId: String): String? {
        return userRepository.getTeamMembers(teamId).find { it.role == UserRole.OWNER }?.id
    }

    fun getEditors(teamId: String): List<String> {
        return userRepository.getTeamMembers(teamId)
            .filter { it.role == UserRole.EDITOR }
            .map { it.id }
    }
}
