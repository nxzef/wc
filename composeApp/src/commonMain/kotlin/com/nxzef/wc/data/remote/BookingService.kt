package com.nxzef.wc.data.remote

import com.nxzef.wc.data.session.SessionManager
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

    suspend fun getAll(): List<Booking> =
        client.get("${ApiClient.BASE_URL}/bookings") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()

    suspend fun getById(id: String): Booking =
        client.get("${ApiClient.BASE_URL}/bookings/$id") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()

    suspend fun getByPhotographer(id: String): List<Booking> =
        client.get(
            "${ApiClient.BASE_URL}/bookings/photographer/$id"
        ) {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()

    suspend fun getByEditor(id: String): List<Booking> =
        client.get(
            "${ApiClient.BASE_URL}/bookings/editor/$id"
        ) {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()

    suspend fun create(request: CreateBookingRequest): Booking =
        client.post("${ApiClient.BASE_URL}/bookings") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun update(
        id: String,
        request: UpdateBookingRequest
    ): Booking =
        client.put("${ApiClient.BASE_URL}/bookings/$id") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}