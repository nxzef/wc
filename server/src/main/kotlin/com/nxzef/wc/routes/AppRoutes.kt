package com.nxzef.wc.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object AppConfigTable : Table("app_config") {
    val key = varchar("key", 100)
    val value = text("value")
    override val primaryKey = PrimaryKey(key)
}

fun Route.appRoutes() {
    get("/app/version") {
        val config = transaction {
            AppConfigTable.selectAll()
                .associate { it[AppConfigTable.key] to it[AppConfigTable.value] }
        }
        call.respond(mapOf(
            "latest_version" to (config["latest_version"] ?: "1.0.0"),
            "download_url" to (config["download_url"] ?: "")
        ))
    }
}
