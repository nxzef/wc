package com.nxzef.wc.data.repository

import com.nxzef.wc.data.remote.BookingService
import com.nxzef.wc.domain.repository.BookingRepository
import com.nxzef.wc.shared.model.Booking
import com.nxzef.wc.shared.model.CreateBookingRequest
import com.nxzef.wc.shared.model.UpdateBookingRequest
import com.nxzef.wc.shared.util.AppResult

class BookingRepositoryImpl(
    private val service: BookingService
) : BookingRepository {

    override suspend fun getAll(): AppResult<List<Booking>> {
        return try {
            AppResult.Success(service.getAll())
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun getById(id: String): AppResult<Booking> {
        return try {
            AppResult.Success(service.getById(id))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun getByPhotographer(id: String): AppResult<List<Booking>> {
        return try {
            AppResult.Success(service.getByPhotographer(id))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun getByEditor(id: String): AppResult<List<Booking>> {
        return try {
            AppResult.Success(service.getByEditor(id))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun create(request: CreateBookingRequest): AppResult<Booking> {
        return try {
            AppResult.Success(service.create(request))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun update(
        id: String,
        request: UpdateBookingRequest
    ): AppResult<Booking> {
        return try {
            AppResult.Success(service.update(id, request))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }
}