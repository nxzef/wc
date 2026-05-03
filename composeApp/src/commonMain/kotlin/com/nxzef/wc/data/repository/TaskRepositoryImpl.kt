package com.nxzef.wc.data.repository

import com.nxzef.wc.data.remote.TaskService
import com.nxzef.wc.domain.repository.TaskRepository
import com.nxzef.wc.shared.model.CreateTaskRequest
import com.nxzef.wc.shared.model.Task
import com.nxzef.wc.shared.util.AppResult

class TaskRepositoryImpl(
    private val service: TaskService
) : TaskRepository {

    override suspend fun getActiveCountByLeadId(leadId: String): AppResult<Int> {
        return try {
            AppResult.Success(service.getActiveCountByLeadId(leadId))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun getMyByLeadId(leadId: String): AppResult<List<Task>> {
        return try {
            AppResult.Success(service.getMyByLeadId(leadId))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun getByLeadId(leadId: String): AppResult<List<Task>> {
        return try {
            AppResult.Success(service.getByLeadId(leadId))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun getByBookingId(bookingId: String): AppResult<List<Task>> {
        return try {
            AppResult.Success(service.getByBookingId(bookingId))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun getMyByBookingId(bookingId: String): AppResult<List<Task>> {
        return try {
            AppResult.Success(service.getMyByBookingId(bookingId))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun getMyPending(): AppResult<List<Task>> {
        return try {
            AppResult.Success(service.getMyPending())
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun getByAssignedUser(userId: String): AppResult<List<Task>> {
        return try {
            AppResult.Success(service.getByAssignedUser(userId))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun create(request: CreateTaskRequest): AppResult<Task> {
        return try {
            AppResult.Success(service.create(request))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun markDone(id: String, done: Boolean): AppResult<Task> {
        return try {
            AppResult.Success(service.markDone(id, done))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun delete(id: String): AppResult<Unit> {
        return try {
            service.delete(id)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }
}