package com.nxzef.wc.domain.repository

import com.nxzef.wc.shared.model.Booking
import com.nxzef.wc.shared.model.CreateBookingRequest
import com.nxzef.wc.shared.model.UpdateBookingRequest
import com.nxzef.wc.shared.util.AppResult

interface BookingRepository {
    suspend fun getAll(): AppResult<List<Booking>>
    suspend fun getById(id: String): AppResult<Booking>
    suspend fun getByPhotographer(id: String): AppResult<List<Booking>>
    suspend fun getByEditor(id: String): AppResult<List<Booking>>
    suspend fun create(request: CreateBookingRequest): AppResult<Booking>
    suspend fun update(id: String, request: UpdateBookingRequest): AppResult<Booking>
}