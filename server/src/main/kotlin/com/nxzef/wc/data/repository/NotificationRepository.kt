package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.NotificationsTable
import com.nxzef.wc.shared.model.CreateNotificationRequest
import com.nxzef.wc.shared.model.Notification
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

class NotificationRepository {

    private fun rowToNotification(row: ResultRow): Notification {
        return Notification(
            id = row[NotificationsTable.id].toString(),
            userId = row[NotificationsTable.userId].toString(),
            title = row[NotificationsTable.title],
            message = row[NotificationsTable.message],
            isRead = row[NotificationsTable.isRead],
            bookingId = row[NotificationsTable.bookingId]?.toString(),
            taskId = row[NotificationsTable.taskId]?.toString(),
            createdAt = row[NotificationsTable.createdAt].toString()
        )
    }

    fun getByUserId(userId: String): List<Notification> {
        return transaction {
            NotificationsTable
                .selectAll()
                .where {
                    NotificationsTable.userId eq
                            java.util.UUID.fromString(userId)
                }
                .orderBy(
                    NotificationsTable.createdAt,
                    SortOrder.DESC
                )
                .map { rowToNotification(it) }
        }
    }

    fun getUnreadCount(userId: String): Int {
        return transaction {
            NotificationsTable
                .selectAll()
                .where {
                    (NotificationsTable.userId eq
                            java.util.UUID.fromString(userId)) and
                            (NotificationsTable.isRead eq false)
                }
                .count().toInt()
        }
    }

    fun create(request: CreateNotificationRequest): Notification {
        return transaction {
            val id = NotificationsTable.insert {
                it[userId] = java.util.UUID.fromString(
                    request.userId
                )
                it[title] = request.title
                it[message] = request.message
                it[isRead] = false
                it[bookingId] = request.bookingId?.let { b ->
                    java.util.UUID.fromString(b)
                }
                it[taskId] = request.taskId?.let { t ->
                    java.util.UUID.fromString(t)
                }
                it[createdAt] = Instant.now()
            } get NotificationsTable.id

            NotificationsTable
                .selectAll()
                .where { NotificationsTable.id eq id }
                .single()
                .let { rowToNotification(it) }
        }
    }

    fun markAsRead(notificationId: String): Boolean {
        return transaction {
            val updated = NotificationsTable.update(
                {
                    NotificationsTable.id eq
                            java.util.UUID.fromString(notificationId)
                }
            ) {
                it[isRead] = true
            }
            updated > 0
        }
    }

    fun markAllAsRead(userId: String): Int {
        return transaction {
            NotificationsTable.update(
                {
                    NotificationsTable.userId eq
                            java.util.UUID.fromString(userId)
                }
            ) {
                it[isRead] = true
            }
        }
    }
}