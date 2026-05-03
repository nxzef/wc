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

    fun getActiveCountByLeadId(leadId: String): Int {
        return transaction {
            TasksTable.selectAll()
                .where {
                    (TasksTable.leadId eq UUID.fromString(leadId)) and
                    (TasksTable.isDone eq false)
                }
                .count().toInt()
        }
    }

    fun getByLeadId(leadId: String): List<Task> {
        return transaction {
            TasksTable.selectAll()
                .where { TasksTable.leadId eq UUID.fromString(leadId) }
                .orderBy(TasksTable.createdAt, SortOrder.ASC)
                .map { rowToTask(it) }
        }
    }

    fun getByMyLeadId(userId: String, leadId: String): List<Task> {
        return transaction {
            TasksTable.selectAll()
                .where {
                    (TasksTable.leadId eq UUID.fromString(leadId)) and
                    (TasksTable.assignedTo eq UUID.fromString(userId))
                }
                .orderBy(TasksTable.createdAt, SortOrder.ASC)
                .map { rowToTask(it) }
        }
    }

    fun getByBookingId(bookingId: String): List<Task> {
        return transaction {
            TasksTable.selectAll()
                .where { TasksTable.bookingId eq UUID.fromString(bookingId) }
                .orderBy(TasksTable.createdAt, SortOrder.ASC)
                .map { rowToTask(it) }
        }
    }

    fun getByMyBookingId(userId: String, bookingId: String): List<Task> {
        return transaction {
            TasksTable.selectAll()
                .where {
                    (TasksTable.bookingId eq UUID.fromString(bookingId)) and
                    (TasksTable.assignedTo eq UUID.fromString(userId))
                }
                .orderBy(TasksTable.createdAt, SortOrder.ASC)
                .map { rowToTask(it) }
        }
    }

    fun getByAssignedUser(userId: String): List<Task> {
        return transaction {
            TasksTable.selectAll()
                .where { TasksTable.assignedTo eq UUID.fromString(userId) }
                .orderBy(TasksTable.dueDate, SortOrder.ASC)
                .map { rowToTask(it) }
        }
    }

    fun getPendingByUser(userId: String): List<Task> {
        return transaction {
            TasksTable.selectAll()
                .where {
                    (TasksTable.assignedTo eq UUID.fromString(userId)) and
                    (TasksTable.isDone eq false)
                }
                .orderBy(TasksTable.dueDate, SortOrder.ASC)
                .map { rowToTask(it) }
        }
    }

    fun create(request: CreateTaskRequest, createdByUserId: String): Task {
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
                it[createdAt]   = Instant.now()
                it[stageName]   = request.stageName
            } get TasksTable.id

            TasksTable.selectAll()
                .where { TasksTable.id eq newId }
                .single()
                .let { rowToTask(it) }
        }
    }

    fun markDone(id: String, done: Boolean): Task? {
        return transaction {
            TasksTable.update({ TasksTable.id eq UUID.fromString(id) }) {
                it[isDone] = done
                it[doneAt] = if (done) Instant.now() else null
            }
            TasksTable.selectAll()
                .where { TasksTable.id eq UUID.fromString(id) }
                .singleOrNull()
                ?.let { rowToTask(it) }
        }
    }

    fun delete(id: String) {
        transaction {
            TasksTable.deleteWhere { TasksTable.id eq UUID.fromString(id) }
        }
    }
}
