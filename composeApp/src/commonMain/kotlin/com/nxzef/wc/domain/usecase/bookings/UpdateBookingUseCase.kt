package com.nxzef.wc.domain.usecase.bookings

import com.nxzef.wc.domain.repository.BookingRepository
import com.nxzef.wc.shared.model.UpdateBookingRequest

class UpdateBookingUseCase(
    private val repository: BookingRepository
) {
    suspend operator fun invoke(
        id: String,
        request: UpdateBookingRequest
    ) = repository.update(id, request)
}