package com.juanpvivas.aichatjp

import android.app.Application
import com.juanpvivas.aichatjp.di.androidContext
import com.juanpvivas.aichatjp.di.initKoin

class AIChaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        androidContext = this
        initKoin()
    }
}
