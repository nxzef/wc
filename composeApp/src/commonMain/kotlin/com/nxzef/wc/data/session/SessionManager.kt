package com.nxzef.wc.data.session

import com.nxzef.wc.data.model.User

object SessionManager {
    var token: String? = null
    var currentUser: User? = null

    val isLoggedIn get() = token != null

    val role get() = currentUser?.role

    fun save(token: String, user: User) {
        this.token = token
        this.currentUser = user
    }

    fun clear() {
        token = null
        currentUser = null
    }
}