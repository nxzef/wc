package com.nxzef.wc.domain.usecase.tasks

import com.nxzef.wc.domain.repository.TaskRepository
import com.nxzef.wc.shared.util.AppResult

class DeleteTaskUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(id: String): AppResult<Unit> {
        return repository.delete(id)
    }
}