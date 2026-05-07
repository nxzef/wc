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
import java.util.UUID

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

    fun getByUserId(userId: String, teamId: String): List<Notification> {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return emptyList() }
        return transaction {
            NotificationsTable
                .selectAll()
                .where {
                    (NotificationsTable.userId eq UUID.fromString(userId)) and
                            (NotificationsTable.teamId eq tUuid)
                }
                .orderBy(NotificationsTable.createdAt, SortOrder.DESC)
                .map { rowToNotification(it) }
        }
    }

    fun getUnreadCount(userId: String, teamId: String): Int {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return 0 }
        return transaction {
            NotificationsTable
                .selectAll()
                .where {
                    (NotificationsTable.userId eq UUID.fromString(userId)) and
                            (NotificationsTable.isRead eq false) and
                            (NotificationsTable.teamId eq tUuid)
                }
                .count().toInt()
        }
    }

    fun create(request: CreateNotificationRequest, teamId: String): Notification {
        val tUuid = UUID.fromString(teamId)
        return transaction {
            val id = NotificationsTable.insert {
                it[userId] = UUID.fromString(request.userId)
                it[title] = request.title
                it[message] = request.message
                it[isRead] = false
                it[bookingId] = request.bookingId?.let { b -> UUID.fromString(b) }
                it[taskId] = request.taskId?.let { t -> UUID.fromString(t) }
                it[NotificationsTable.teamId] = tUuid
                it[createdAt] = Instant.now()
            } get NotificationsTable.id

            NotificationsTable
                .selectAll()
                .where { NotificationsTable.id eq id }
                .single()
                .let { rowToNotification(it) }
        }
    }

    fun markAsRead(notificationId: String, teamId: String): Boolean {
        val tUuid = UUID.fromString(teamId)
        return transaction {
            val updated = NotificationsTable.update(
                {
                    (NotificationsTable.id eq UUID.fromString(notificationId)) and
                            (NotificationsTable.teamId eq tUuid)
                }
            ) {
                it[isRead] = true
            }
            updated > 0
        }
    }

    fun markAllAsRead(userId: String, teamId: String): Int {
        val tUuid = UUID.fromString(teamId)
        return transaction {
            NotificationsTable.update(
                {
                    (NotificationsTable.userId eq UUID.fromString(userId)) and
                            (NotificationsTable.teamId eq tUuid)
                }
            ) {
                it[isRead] = true
            }
        }
    }
}
