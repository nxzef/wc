package com.nxzef.wc.di

import com.nxzef.wc.data.remote.ApiService
import org.koin.dsl.module

val serviceModule = module {
    single<ApiService> { ApiService() }
}