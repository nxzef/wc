package com.nxzef.wc.data.remote

import com.nxzef.wc.config.AppConfig
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.shared.dto.BookingDto
import com.nxzef.wc.shared.dto.toDomain
import com.nxzef.wc.shared.model.Booking
import com.nxzef.wc.shared.model.CreateBookingRequest
import com.nxzef.wc.shared.model.UpdateBookingRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class BookingService(private val client: HttpClient) {

    suspend fun getAll(): List<Booking> {
        val dtos: List<BookingDto> = client.get("${AppConfig.BASE_URL}/bookings") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()
        return dtos.map { it.toDomain() }
    }

    suspend fun getById(id: String): Booking {
        val dto: BookingDto = client.get("${AppConfig.BASE_URL}/bookings/$id") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()
        return dto.toDomain()
    }

    suspend fun getByPhotographer(id: String): List<Booking> {
        val dtos: List<BookingDto> = client.get(
            "${AppConfig.BASE_URL}/bookings/photographer/$id"
        ) {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()
        return dtos.map { it.toDomain() }
    }

    suspend fun getByEditor(id: String): List<Booking> {
        val dtos: List<BookingDto> = client.get(
            "${AppConfig.BASE_URL}/bookings/editor/$id"
        ) {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()
        return dtos.map { it.toDomain() }
    }

    suspend fun create(request: CreateBookingRequest): Booking {
        val dto: BookingDto = client.post("${AppConfig.BASE_URL}/bookings") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        return dto.toDomain()
    }

    suspend fun update(
        id: String,
        request: UpdateBookingRequest
    ): Booking {
        val dto: BookingDto = client.put("${AppConfig.BASE_URL}/bookings/$id") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        return dto.toDomain()
    }
}