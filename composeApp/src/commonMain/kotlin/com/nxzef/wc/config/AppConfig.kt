package com.nxzef.wc.config

object AppConfig {
    const val IS_PRODUCTION = true

    val BASE_URL get() = if (IS_PRODUCTION) {
        "https://wc-server-production-f8e9.up.railway.app"
    } else {
        "http://localhost:8080"
    }
}
