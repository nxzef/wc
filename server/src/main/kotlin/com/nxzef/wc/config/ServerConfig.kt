package com.nxzef.wc.config

object ServerConfig {
    val isProduction get() = System.getenv("ENVIRONMENT") == "production"
    val jwtSecret get() = System.getenv("JWT_SECRET") ?: "debug_secret_for_tests_only"
    val databaseUrl get() = System.getenv("DATABASE_URL") ?: "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
    val resendApiKey get() = System.getenv("RESEND_API_KEY") ?: ""
    val fromEmail get() = System.getenv("FROM_EMAIL") ?: "noreply@weddingclouds.com"
}
