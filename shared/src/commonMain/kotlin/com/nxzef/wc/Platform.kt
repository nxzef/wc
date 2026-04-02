package com.nxzef.wc

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform