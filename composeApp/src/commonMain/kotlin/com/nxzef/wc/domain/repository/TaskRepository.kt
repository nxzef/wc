package com.nxzef.wc.domain.repository

import com.nxzef.wc.shared.model.CreateTaskRequest
import com.nxzef.wc.shared.model.Task

interface TaskRepository {
    suspend fun getByLeadId(leadId: String): Result<List<Task>>
    suspend fun getByBookingId(bookingId: String): Result<List<Task>>
    suspend fun getMyPending(): Result<List<Task>>
    suspend fun create(request: CreateTaskRequest): Result<Task>
    suspend fun markDone(id: String, done: Boolean): Result<Task>
}