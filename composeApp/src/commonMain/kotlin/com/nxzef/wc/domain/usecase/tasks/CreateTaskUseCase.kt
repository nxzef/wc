package com.nxzef.wc.domain.usecase.tasks

import com.nxzef.wc.domain.repository.TaskRepository
import com.nxzef.wc.shared.model.CreateTaskRequest
import com.nxzef.wc.shared.model.Task
import com.nxzef.wc.shared.util.AppResult

class CreateTaskUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(request: CreateTaskRequest): AppResult<Task> {
        return repository.create(request)
    }
}