package com.nxzef.wc

import android.app.Application
import com.nxzef.wc.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class WCApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@WCApp)
        }
    }
}