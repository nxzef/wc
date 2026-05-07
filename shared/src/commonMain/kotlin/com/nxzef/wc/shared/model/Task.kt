package com.nxzef.wc.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id          : String,
    val leadId      : String?  = null,
    val bookingId   : String?  = null,
    val title       : String,
    val description : String?  = null,
    val assignedTo  : String,
    val dueDate     : String?  = null,
    val isDone      : Boolean,
    val doneAt      : String?  = null,
    val createdBy   : String,
    val createdAt   : String,
    val stageName   : String?  = null
)

@Serializable
data class CreateTaskRequest(
    val leadId      : String?  = null,
    val bookingId   : String?  = null,
    val title       : String,
    val description : String?  = null,
    val dueDate     : String?  = null,
    val stageName   : String?  = null
)

@Serializable
data class UpdateTaskRequest(
    val isDone      : Boolean,
    val doneAt      : String?  = null
)
