package com.afi.gemichat.ui.enums

import com.afi.gemichat.ui.util.Preferences
import com.afi.gemichat.ui.util.THEME_MODE

enum class ThemeMode {
    SYSTEM, LIGHT, DARK;

    companion object {
        val current : ThemeMode = valueOf(Preferences.getString(THEME_MODE, SYSTEM.name))
    }
}