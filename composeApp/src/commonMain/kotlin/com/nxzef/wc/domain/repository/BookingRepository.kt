package com.nxzef.wc.domain.repository

import com.nxzef.wc.shared.model.Booking
import com.nxzef.wc.shared.model.CreateBookingRequest
import com.nxzef.wc.shared.model.UpdateBookingRequest

interface BookingRepository {
    suspend fun getAll(): Result<List<Booking>>
    suspend fun getById(id: String): Result<Booking>
    suspend fun getByPhotographer(id: String): Result<List<Booking>>
    suspend fun getByEditor(id: String): Result<List<Booking>>
    suspend fun create(request: CreateBookingRequest): Result<Booking>
    suspend fun update(id: String, request: UpdateBookingRequest): Result<Booking>
}