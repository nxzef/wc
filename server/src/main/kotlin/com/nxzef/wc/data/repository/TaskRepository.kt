package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.TasksTable
import com.nxzef.wc.shared.model.CreateTaskRequest
import com.nxzef.wc.shared.model.Task
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.time.LocalDate

class TaskRepository {

    private fun rowToTask(row: ResultRow): Task {
        return Task(
            id = row[TasksTable.id].toString(),
            leadId = row[TasksTable.leadId]?.toString(),
            bookingId = row[TasksTable.bookingId]?.toString(),
            title = row[TasksTable.title],
            description = row[TasksTable.description],
            assignedTo = row[TasksTable.assignedTo].toString(),
            dueDate = row[TasksTable.dueDate]?.toString(),
            isDone = row[TasksTable.isDone],
            doneAt = row[TasksTable.doneAt]?.toString(),
            createdBy = row[TasksTable.createdBy].toString(),
            createdAt = row[TasksTable.createdAt].toString()
        )
    }

    fun getByLeadId(leadId: String): List<Task> {
        return transaction {
            TasksTable
                .selectAll()
                .where {
                    TasksTable.leadId eq
                            java.util.UUID.fromString(leadId)
                }
                .orderBy(TasksTable.createdAt, SortOrder.ASC)
                .map { rowToTask(it) }
        }
    }

    fun getByBookingId(bookingId: String): List<Task> {
        return transaction {
            TasksTable
                .selectAll()
                .where {
                    TasksTable.bookingId eq
                            java.util.UUID.fromString(bookingId)
                }
                .orderBy(TasksTable.createdAt, SortOrder.ASC)
                .map { rowToTask(it) }
        }
    }

    fun getByAssignedUser(userId: String): List<Task> {
        return transaction {
            TasksTable
                .selectAll()
                .where {
                    TasksTable.assignedTo eq
                            java.util.UUID.fromString(userId)
                }
                .orderBy(TasksTable.dueDate, SortOrder.ASC)
                .map { rowToTask(it) }
        }
    }

    fun getPendingByUser(userId: String): List<Task> {
        return transaction {
            TasksTable
                .selectAll()
                .where {
                    (TasksTable.assignedTo eq
                            java.util.UUID.fromString(userId)) and
                            (TasksTable.isDone eq false)
                }
                .orderBy(TasksTable.dueDate, SortOrder.ASC)
                .map { rowToTask(it) }
        }
    }

    fun create(
        request: CreateTaskRequest,
        createdByUserId: String
    ): Task {
        return transaction {
            val id = TasksTable.insert {
                it[leadId] = request.leadId?.let { l ->
                    java.util.UUID.fromString(l)
                }
                it[bookingId] = request.bookingId?.let { b ->
                    java.util.UUID.fromString(b)
                }
                it[title] = request.title
                it[description] = request.description
                it[assignedTo] = java.util.UUID.fromString(
                    request.assignedTo
                )
                it[dueDate] = request.dueDate?.let { d ->
                    LocalDate.parse(d)
                }
                it[isDone] = false
                it[createdBy] = java.util.UUID.fromString(
                    createdByUserId
                )
                it[createdAt] = Instant.now()
            } get TasksTable.id

            TasksTable
                .selectAll()
                .where { TasksTable.id eq id }
                .single()
                .let { rowToTask(it) }
        }
    }

    fun markDone(id: String, done: Boolean): Task? {
        return transaction {
            TasksTable.update(
                { TasksTable.id eq java.util.UUID.fromString(id) }
            ) {
                it[isDone] = done
                it[doneAt] = if (done) Instant.now() else null
            }
            TasksTable
                .selectAll()
                .where {
                    TasksTable.id eq
                            java.util.UUID.fromString(id)
                }
                .singleOrNull()
                ?.let { rowToTask(it) }
        }
    }

    // Auto-create default tasks when lead is created
    fun createDefaultLeadTasks(
        leadId: String,
        assignedTo: String,
        createdBy: String
    ) {
        val defaultTasks = listOf(
            "Contact the lead",
            "Understand requirements",
            "Send quote",
            "Follow up"
        )
        transaction {
            defaultTasks.forEachIndexed { index, taskTitle ->
                TasksTable.insert {
                    it[TasksTable.leadId] =
                        java.util.UUID.fromString(leadId)
                    it[title] = taskTitle
                    it[TasksTable.assignedTo] =
                        java.util.UUID.fromString(assignedTo)
                    it[dueDate] = LocalDate.now().plusDays((index + 1).toLong())
                    it[isDone] = false
                    it[TasksTable.createdBy] =
                        java.util.UUID.fromString(createdBy)
                    it[createdAt] = Instant.now()
                }
            }
        }
    }

    // Auto-create default tasks when booking is created
    fun createDefaultBookingTasks(
        bookingId: String,
        assignedTo: String,
        createdBy: String
    ) {
        val defaultTasks = listOf(
            "Collect advance payment",
            "Send contract",
            "Assign photographer",
            "Confirm venue details",
            "Shoot day completed",
            "Edit photos",
            "Deliver gallery",
            "Collect final payment"
        )
        transaction {
            defaultTasks.forEachIndexed { index, taskTitle ->
                TasksTable.insert {
                    it[TasksTable.bookingId] =
                        java.util.UUID.fromString(bookingId)
                    it[title] = taskTitle
                    it[TasksTable.assignedTo] =
                        java.util.UUID.fromString(assignedTo)
                    it[dueDate] = LocalDate.now().plusDays((index + 1).toLong())
                    it[isDone] = false
                    it[TasksTable.createdBy] =
                        java.util.UUID.fromString(createdBy)
                    it[createdAt] = Instant.now()
                }
            }
        }
    }
}