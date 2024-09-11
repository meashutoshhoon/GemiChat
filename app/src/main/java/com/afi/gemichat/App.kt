package com.afi.gemichat

import android.app.Application
import com.afi.gemichat.ui.util.Preferences
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        applicationScope = CoroutineScope(SupervisorJob())
        Preferences.init(this)
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    companion object {
        lateinit var applicationScope: CoroutineScope
    }
}