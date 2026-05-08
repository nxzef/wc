package com.nxzef.wc.config

object AppConfig {
    const val IS_PRODUCTION = true

    val BASE_URL get() = if (IS_PRODUCTION) {
        "https://wc-server.up.railway.app"
    } else {
        "http://localhost:8080"
    }
}
