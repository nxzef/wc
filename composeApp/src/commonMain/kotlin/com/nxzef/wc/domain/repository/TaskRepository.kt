package com.nxzef.wc.domain.repository

import com.nxzef.wc.shared.model.CreateTaskRequest
import com.nxzef.wc.shared.model.Task
import com.nxzef.wc.shared.util.AppResult

interface TaskRepository {
    suspend fun getByLeadId(leadId: String): AppResult<List<Task>>
    suspend fun getByBookingId(bookingId: String): AppResult<List<Task>>
    suspend fun getMyPending(): AppResult<List<Task>>
    suspend fun getByAssignedUser(userId: String): AppResult<List<Task>>
    suspend fun create(request: CreateTaskRequest): AppResult<Task>
    suspend fun markDone(id: String, done: Boolean): AppResult<Task>
}