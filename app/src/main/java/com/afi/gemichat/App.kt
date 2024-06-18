package com.afi.gemichat

import android.app.Application
import com.afi.gemichat.ui.util.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        applicationScope = CoroutineScope(SupervisorJob())
        Preferences.init(this)
    }

    companion object {
        lateinit var applicationScope: CoroutineScope
    }
}