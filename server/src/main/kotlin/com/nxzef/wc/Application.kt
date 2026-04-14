package com.nxzef.wc

import com.nxzef.wc.data.db.DatabaseFactory
import com.nxzef.wc.plugins.configureRouting
import com.nxzef.wc.plugins.configureSecurity
import com.nxzef.wc.plugins.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()
    configureSerialization()
    configureSecurity()
    configureRouting()
}