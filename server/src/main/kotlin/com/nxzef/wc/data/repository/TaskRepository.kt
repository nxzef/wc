package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.TasksTable
import com.nxzef.wc.shared.model.CreateTaskRequest
import com.nxzef.wc.shared.model.Task
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class TaskRepository {

    private fun rowToTask(row: ResultRow): Task = Task(
        id          = row[TasksTable.id].toString(),
        leadId      = row[TasksTable.leadId]?.toString(),
        bookingId   = row[TasksTable.bookingId]?.toString(),
        title       = row[TasksTable.title],
        description = row[TasksTable.description],
        assignedTo  = row[TasksTable.assignedTo].toString(),
        dueDate     = row[TasksTable.dueDate]?.toString(),
        isDone      = row[TasksTable.isDone],
        doneAt      = row[TasksTable.doneAt]?.toString(),
        createdBy   = row[TasksTable.createdBy].toString(),
        createdAt   = row[TasksTable.createdAt].toString(),
        stageName   = row[TasksTable.stageName]
    )

    fun getActiveCountByLeadId(leadId: String, teamId: String): Int {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return 0 }
        return transaction {
            TasksTable.selectAll()
                .where {
                    (TasksTable.leadId eq UUID.fromString(leadId)) and
                    (TasksTable.isDone eq false) and
                    (TasksTable.teamId eq tUuid)
                }
                .count().toInt()
        }
    }

    fun getByLeadId(leadId: String, teamId: String): List<Task> {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return emptyList() }
        return transaction {
            TasksTable.selectAll()
                .where {
                    (TasksTable.leadId eq UUID.fromString(leadId)) and
                    (TasksTable.teamId eq tUuid)
                }
                .orderBy(TasksTable.createdAt, SortOrder.ASC)
                .map { rowToTask(it) }
        }
    }

    fun getByMyLeadId(userId: String, leadId: String, teamId: String): List<Task> {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return emptyList() }
        return transaction {
            TasksTable.selectAll()
                .where {
                    (TasksTable.leadId eq UUID.fromString(leadId)) and
                    (TasksTable.assignedTo eq UUID.fromString(userId)) and
                    (TasksTable.teamId eq tUuid)
                }
                .orderBy(TasksTable.createdAt, SortOrder.ASC)
                .map { rowToTask(it) }
        }
    }

    fun getByBookingId(bookingId: String, teamId: String): List<Task> {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return emptyList() }
        return transaction {
            TasksTable.selectAll()
                .where {
                    (TasksTable.bookingId eq UUID.fromString(bookingId)) and
                    (TasksTable.teamId eq tUuid)
                }
                .orderBy(TasksTable.createdAt, SortOrder.ASC)
                .map { rowToTask(it) }
        }
    }

    fun getByMyBookingId(userId: String, bookingId: String, teamId: String): List<Task> {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return emptyList() }
        return transaction {
            TasksTable.selectAll()
                .where {
                    (TasksTable.bookingId eq UUID.fromString(bookingId)) and
                    (TasksTable.assignedTo eq UUID.fromString(userId)) and
                    (TasksTable.teamId eq tUuid)
                }
                .orderBy(TasksTable.createdAt, SortOrder.ASC)
                .map { rowToTask(it) }
        }
    }

    fun getByAssignedUser(userId: String, teamId: String): List<Task> {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return emptyList() }
        return transaction {
            TasksTable.selectAll()
                .where {
                    (TasksTable.assignedTo eq UUID.fromString(userId)) and
                    (TasksTable.teamId eq tUuid)
                }
                .orderBy(TasksTable.dueDate, SortOrder.ASC)
                .map { rowToTask(it) }
        }
    }

    fun getPendingByUser(userId: String, teamId: String): List<Task> {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return emptyList() }
        return transaction {
            TasksTable.selectAll()
                .where {
                    (TasksTable.assignedTo eq UUID.fromString(userId)) and
                    (TasksTable.isDone eq false) and
                    (TasksTable.teamId eq tUuid)
                }
                .orderBy(TasksTable.dueDate, SortOrder.ASC)
                .map { rowToTask(it) }
        }
    }

    fun create(request: CreateTaskRequest, createdByUserId: String, teamId: String): Task {
        val tUuid = UUID.fromString(teamId)
        return transaction {
            val newId = TasksTable.insert {
                it[leadId]      = request.leadId?.let { l -> UUID.fromString(l) }
                it[bookingId]   = request.bookingId?.let { b -> UUID.fromString(b) }
                it[title]       = request.title
                it[description] = request.description
                it[assignedTo]  = UUID.fromString(createdByUserId)
                it[dueDate]     = request.dueDate?.let { d -> LocalDate.parse(d) }
                it[isDone]      = false
                it[createdBy]   = UUID.fromString(createdByUserId)
                it[stageName]   = request.stageName
                it[TasksTable.teamId] = tUuid
                it[createdAt]   = Instant.now()
            } get TasksTable.id

            TasksTable.selectAll()
                .where { TasksTable.id eq newId }
                .single()
                .let { rowToTask(it) }
        }
    }

    fun markDone(id: String, done: Boolean, teamId: String): Task? {
        val tUuid = UUID.fromString(teamId)
        return transaction {
            TasksTable.update({
                (TasksTable.id eq UUID.fromString(id)) and (TasksTable.teamId eq tUuid)
            }) {
                it[isDone] = done
                it[doneAt] = if (done) Instant.now() else null
            }
            TasksTable.selectAll()
                .where { (TasksTable.id eq UUID.fromString(id)) and (TasksTable.teamId eq tUuid) }
                .singleOrNull()
                ?.let { rowToTask(it) }
        }
    }

    fun delete(id: String, teamId: String) {
        val tUuid = UUID.fromString(teamId)
        transaction {
            TasksTable.deleteWhere {
                (TasksTable.id eq UUID.fromString(id)) and (TasksTable.teamId eq tUuid)
            }
        }
    }
}
