package com.nxzef.wc.domain.usecase.bookings

import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.domain.repository.BookingRepository
import com.nxzef.wc.shared.util.AppResult

class GetMyEditingQueueUseCase(
    private val repository: BookingRepository
) {
    suspend operator fun invoke() =
        SessionManager.getUser()?.id?.let {
            repository.getByEditor(it)
        } ?: AppResult.Success(emptyList())
}