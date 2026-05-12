package com.nxzef.wc.config

object AppConfig {
    const val IS_PRODUCTION = true
    const val CURRENT_VERSION = "1.0.0"

    val BASE_URL get() = if (IS_PRODUCTION) {
        "https://wc-server-production-f8e9.up.railway.app"
    } else {
        "http://localhost:8080"
    }
}
