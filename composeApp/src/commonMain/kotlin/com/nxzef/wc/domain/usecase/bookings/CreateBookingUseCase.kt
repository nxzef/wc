package com.nxzef.wc.domain.usecase.bookings

import com.nxzef.wc.domain.repository.BookingRepository
import com.nxzef.wc.shared.model.CreateBookingRequest

class CreateBookingUseCase(
    private val repository: BookingRepository
) {
    suspend operator fun invoke(
        request: CreateBookingRequest
    ) = repository.create(request)
}