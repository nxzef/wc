package com.nxzef.wc.data.repository

import com.nxzef.wc.data.remote.TaskService
import com.nxzef.wc.domain.repository.TaskRepository
import com.nxzef.wc.shared.model.CreateTaskRequest

class TaskRepositoryImpl(
    private val service: TaskService
) : TaskRepository {

    override suspend fun getByLeadId(leadId: String) =
        runCatching { service.getByLeadId(leadId) }

    override suspend fun getByBookingId(bookingId: String) =
        runCatching { service.getByBookingId(bookingId) }

    override suspend fun getMyPending() =
        runCatching { service.getMyPending() }

    override suspend fun create(request: CreateTaskRequest) =
        runCatching { service.create(request) }

    override suspend fun markDone(id: String, done: Boolean) =
        runCatching { service.markDone(id, done) }
}