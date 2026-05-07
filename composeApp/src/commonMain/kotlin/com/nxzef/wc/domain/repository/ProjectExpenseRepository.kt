package com.nxzef.wc.domain.repository

import com.nxzef.wc.shared.model.CreateProjectExpenseRequest
import com.nxzef.wc.shared.model.ProjectExpense
import com.nxzef.wc.shared.util.AppResult

interface ProjectExpenseRepository {
    suspend fun getByBookingId(bookingId: String): AppResult<List<ProjectExpense>>
    suspend fun create(request: CreateProjectExpenseRequest): AppResult<ProjectExpense>
    suspend fun delete(id: String): AppResult<Unit>
}
