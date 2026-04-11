package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.LeadRepository
import com.nxzef.wc.domain.model.CreateLeadRequest
import com.nxzef.wc.domain.model.UpdateLeadStatusRequest
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.leadRoutes(leadRepository: LeadRepository) {
    route("/leads") {

        // GET all leads
        get {
            val leads = leadRepository.getAll()
            call.respond(leads)
        }

        // GET lead by id
        get("/{id}") {
            val id = call.parameters["id"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    "Missing id"
                )
            val lead = leadRepository.getById(id)
                ?: return@get call.respond(
                    HttpStatusCode.NotFound,
                    "Lead not found"
                )
            call.respond(lead)
        }

        // POST create lead
        post {
            val principal = call.principal<JWTPrincipal>()
            val addedBy = principal?.payload
                ?.getClaim("userId")?.asString()
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    "Unauthorized"
                )
            val request = call.receive<CreateLeadRequest>()
            val lead = leadRepository.create(request, addedBy)
            call.respond(HttpStatusCode.Created, lead)
        }

        // PUT update lead status
        put("/{id}/status") {
            val id = call.parameters["id"]
                ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    "Missing id"
                )
            val request = call.receive<UpdateLeadStatusRequest>()
            val lead = leadRepository.updateStatus(id, request)
                ?: return@put call.respond(
                    HttpStatusCode.NotFound,
                    "Lead not found"
                )
            call.respond(lead)
        }
    }
}