package com.nxzef.wc.domain.usecase.tasks

import com.nxzef.wc.domain.repository.TaskRepository
import com.nxzef.wc.shared.model.Task
import com.nxzef.wc.shared.util.AppResult

class GetTasksByLeadUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(leadId: String): AppResult<List<Task>> {
        return repository.getByLeadId(leadId)
    }
}