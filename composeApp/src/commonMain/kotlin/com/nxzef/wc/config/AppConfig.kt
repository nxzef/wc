package com.nxzef.wc.config

object AppConfig {
    const val IS_PRODUCTION = true
    const val CURRENT_VERSION = "1.0.1"

    val BASE_URL get() = if (IS_PRODUCTION) {
        "https://wc-server-1055385353237.asia-south1.run.app"
    } else {
        "http://localhost:8080"
    }
}
