package com.nxzef.wc.data.repository

import com.nxzef.wc.data.remote.BookingService
import com.nxzef.wc.domain.repository.BookingRepository
import com.nxzef.wc.shared.model.CreateBookingRequest
import com.nxzef.wc.shared.model.UpdateBookingRequest

class BookingRepositoryImpl(
    private val service: BookingService
) : BookingRepository {

    override suspend fun getAll() =
        runCatching { service.getAll() }

    override suspend fun getById(id: String) =
        runCatching { service.getById(id) }

    override suspend fun getByPhotographer(id: String) =
        runCatching { service.getByPhotographer(id) }

    override suspend fun getByEditor(id: String) =
        runCatching { service.getByEditor(id) }

    override suspend fun create(request: CreateBookingRequest) =
        runCatching { service.create(request) }

    override suspend fun update(
        id: String,
        request: UpdateBookingRequest
    ) = runCatching { service.update(id, request) }
}