package com.nxzef.wc.data.remote

import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.shared.dto.MonthlyGoalDto
import com.nxzef.wc.shared.dto.toDomain
import com.nxzef.wc.shared.model.MonthlyGoal
import com.nxzef.wc.shared.model.UpsertMonthlyGoalRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class MonthlyGoalService(private val client: HttpClient) {

    suspend fun upsert(request: UpsertMonthlyGoalRequest): MonthlyGoal {
        val dto: MonthlyGoalDto = client.post("${ApiClient.BASE_URL}/goals") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        return dto.toDomain()
    }
}
