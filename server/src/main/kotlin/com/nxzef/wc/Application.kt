package com.nxzef.wc

import com.nxzef.wc.config.ServerConfig
import com.nxzef.wc.data.db.DatabaseFactory
import com.nxzef.wc.di.serverModule
import com.nxzef.wc.plugins.configureRouting
import com.nxzef.wc.plugins.configureSecurity
import com.nxzef.wc.plugins.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(serverModule)
    }
    DatabaseFactory.init()
    logEmailConfig()
    configureSerialization()
    configureSecurity()
    configureRouting()
}

private fun logEmailConfig() {
    val apiKeyState = if (ServerConfig.resendApiKey.isBlank()) {
        "NOT SET"
    } else {
        "SET (${ServerConfig.resendApiKey.length} chars)"
    }
    println("📧 Email config: apiKey=$apiKeyState, from=${ServerConfig.fromEmail}")
}