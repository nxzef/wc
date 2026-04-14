package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.QuoteRepository
import com.nxzef.wc.shared.model.CreateQuoteRequest
import com.nxzef.wc.shared.model.UpdateQuoteStatusRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.quoteRoutes(quoteRepository: QuoteRepository) {
    route("/quotes") {

        // GET quotes by lead id
        get("/lead/{leadId}") {
            val leadId = call.parameters["leadId"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    "Missing leadId"
                )
            val quotes = quoteRepository.getByLeadId(leadId)
            call.respond(quotes)
        }

        // GET quote by id
        get("/{id}") {
            val id = call.parameters["id"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    "Missing id"
                )
            val quote = quoteRepository.getById(id)
                ?: return@get call.respond(
                    HttpStatusCode.NotFound,
                    "Quote not found"
                )
            call.respond(quote)
        }

        // POST create quote
        post {
            val principal = call.principal<JWTPrincipal>()
            val createdBy = principal?.payload
                ?.getClaim("userId")?.asString()
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    "Unauthorized"
                )
            val request = call.receive<CreateQuoteRequest>()
            val quote = quoteRepository.create(request, createdBy)
            call.respond(HttpStatusCode.Created, quote)
        }

        // PUT update quote status
        put("/{id}/status") {
            val id = call.parameters["id"]
                ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    "Missing id"
                )
            val request = call.receive<UpdateQuoteStatusRequest>()
            val quote = quoteRepository.updateStatus(id, request)
                ?: return@put call.respond(
                    HttpStatusCode.NotFound,
                    "Quote not found"
                )
            call.respond(quote)
        }
    }
}