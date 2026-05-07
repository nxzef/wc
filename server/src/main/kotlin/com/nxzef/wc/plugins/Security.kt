package com.nxzef.wc.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.nxzef.wc.config.ServerConfig
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

val jwtSecret get() = ServerConfig.jwtSecret
const val jwtIssuer = "wc-app"
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

fun generateToken(userId: String, email: String, role: String, teamId: String?): String {
    val builder = JWT.create()
        .withIssuer(jwtIssuer)
        .withAudience(jwtAudience)
        .withClaim("userId", userId)
        .withClaim("email", email)
        .withClaim("role", role)
        .withExpiresAt(
            java.util.Date(System.currentTimeMillis() + 86_400_000L)
        )
    if (teamId != null) builder.withClaim("teamId", teamId)
    return builder.sign(Algorithm.HMAC256(jwtSecret))
}

fun generateRefreshToken(): String {
    val bytes = ByteArray(64)
    java.security.SecureRandom().nextBytes(bytes)
    return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
}