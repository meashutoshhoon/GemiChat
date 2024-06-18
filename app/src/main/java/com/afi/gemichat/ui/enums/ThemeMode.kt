package com.afi.gemichat.ui.enums

import com.afi.gemichat.ui.util.Preferences

enum class ThemeMode {
    SYSTEM, LIGHT, DARK;

    companion object {
        fun getCurrent() = valueOf(Preferences.getString(Preferences.themeModeKey, SYSTEM.name))
    }
}