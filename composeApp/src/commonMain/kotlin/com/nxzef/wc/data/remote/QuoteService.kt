package com.nxzef.wc.data.remote

import com.nxzef.wc.config.AppConfig
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.shared.dto.QuoteDto
import com.nxzef.wc.shared.dto.toDomain
import com.nxzef.wc.shared.model.Quote
import com.nxzef.wc.shared.model.SendQuoteRequest
import com.nxzef.wc.shared.model.UpdateQuoteStatusRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class QuoteService(private val client: HttpClient) {

    suspend fun getByLeadId(leadId: String): List<Quote> {
        val dtos: List<QuoteDto> = client.get("${AppConfig.BASE_URL}/quotes/lead/$leadId") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()
        return dtos.map { it.toDomain() }
    }

    suspend fun getById(id: String): Quote {
        val dto: QuoteDto = client.get("${AppConfig.BASE_URL}/quotes/$id") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()
        return dto.toDomain()
    }

    suspend fun sendQuote(request: SendQuoteRequest): Quote {
        val dto: QuoteDto = client.post("${AppConfig.BASE_URL}/quotes/send") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        return dto.toDomain()
    }

    suspend fun updateStatus(id: String, request: UpdateQuoteStatusRequest): Quote {
        val dto: QuoteDto = client.put("${AppConfig.BASE_URL}/quotes/$id/status") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        return dto.toDomain()
    }
}
