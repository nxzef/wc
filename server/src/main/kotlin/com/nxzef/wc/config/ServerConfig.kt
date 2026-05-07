package com.nxzef.wc.config

object ServerConfig {
    val isProduction get() = System.getenv("ENVIRONMENT") == "production"
    val jwtSecret get() = System.getenv("JWT_SECRET") ?: error("JWT_SECRET not set")
    val databaseUrl get() = System.getenv("DATABASE_URL") ?: error("DATABASE_URL not set")
    val resendApiKey get() = System.getenv("RESEND_API_KEY") ?: ""
    val fromEmail get() = System.getenv("FROM_EMAIL") ?: "noreply@weddingclouds.com"
}
