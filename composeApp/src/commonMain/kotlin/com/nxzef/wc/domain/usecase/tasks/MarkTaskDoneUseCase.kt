package com.nxzef.wc.domain.usecase.tasks

import com.nxzef.wc.domain.repository.TaskRepository
import com.nxzef.wc.shared.model.Task
import com.nxzef.wc.shared.util.AppResult

class MarkTaskDoneUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, isDone: Boolean): AppResult<Task> {
        return repository.markDone(taskId, isDone)
    }
}