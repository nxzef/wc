package com.nxzef.wc.domain.usecase.bookings

import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.domain.repository.BookingRepository

class GetMyShootsUseCase(
    private val repository: BookingRepository
) {
    suspend operator fun invoke() =
        SessionManager.getUser()?.id?.let {
            repository.getByPhotographer(it)
        } ?: Result.success(emptyList())
}