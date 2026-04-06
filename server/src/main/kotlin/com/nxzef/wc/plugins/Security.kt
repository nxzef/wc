package com.nxzef.wc.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

val jwtSecret   = System.getenv("JWT_SECRET") ?: "wc-dev-secret-change-in-production"
const val jwtIssuer   = "wc-app"
const val jwtAudience = "wc-users"

fun Application.configureSecurity() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "Wedding Clouds"
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(jwtIssuer)
                    .withAudience(jwtAudience)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("email").asString() != null)
                    JWTPrincipal(credential.payload)
                else null
            }
        }
    }
}

fun generateToken(userId: String, email: String, role: String): String {
    return JWT.create()
        .withIssuer(jwtIssuer)
        .withAudience(jwtAudience)
        .withClaim("userId", userId)
        .withClaim("email", email)
        .withClaim("role", role)
        .withExpiresAt(
            java.util.Date(System.currentTimeMillis() + 86_400_000L)
        )
        .sign(Algorithm.HMAC256(jwtSecret))
}