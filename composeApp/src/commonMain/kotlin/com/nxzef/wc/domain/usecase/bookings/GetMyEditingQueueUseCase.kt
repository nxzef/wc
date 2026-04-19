package com.nxzef.wc.domain.usecase.bookings

import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.domain.repository.BookingRepository

class GetMyEditingQueueUseCase(
    private val repository: BookingRepository
) {
    suspend operator fun invoke() =
        SessionManager.getUser()?.id?.let {
            repository.getByEditor(it)
        } ?: Result.success(emptyList())
}