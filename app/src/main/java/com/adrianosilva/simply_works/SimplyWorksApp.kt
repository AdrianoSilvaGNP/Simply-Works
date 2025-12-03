package com.adrianosilva.simply_works

import android.app.Application
import timber.log.Timber

class SimplyWorksApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}