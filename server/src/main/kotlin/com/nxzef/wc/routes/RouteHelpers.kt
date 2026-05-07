package com.nxzef.wc.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond

/**
 * Extracts userId from the JWT principal, or responds 401 and returns null.
 */
suspend fun ApplicationCall.requireUserId(): String? {
    val id = principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asString()
    if (id.isNullOrBlank()) {
        respond(HttpStatusCode.Unauthorized, "Unauthorized")
        return null
    }
    return id
}

/**
 * Extracts teamId from the JWT principal, or responds 401 and returns null.
 * Every team-scoped route should call this first — there's no meaningful access without a team.
 */
suspend fun ApplicationCall.requireTeamId(): String? {
    val id = principal<JWTPrincipal>()?.payload?.getClaim("teamId")?.asString()
    if (id.isNullOrBlank()) {
        respond(HttpStatusCode.Unauthorized, "No team context")
        return null
    }
    return id
}

/**
 * Extracts the role string from the JWT principal, or null.
 */
fun ApplicationCall.role(): String? =
    principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()
