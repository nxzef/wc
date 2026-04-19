package com.nxzef.wc.domain.usecase.bookings

import com.nxzef.wc.domain.repository.BookingRepository

class GetAllBookingsUseCase(
    private val repository: BookingRepository
) {
    suspend operator fun invoke() = repository.getAll()
}