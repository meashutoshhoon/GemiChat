package com.afi.gemichat.ui.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.afi.gemichat.ui.enums.ThemeMode
import com.afi.gemichat.ui.util.Preferences
import com.afi.gemichat.ui.util.THEME_MODE

class ThemeModel : ViewModel() {
    var themeMode by mutableStateOf(ThemeMode.current)
        private set

    fun updateThemeMode(newMode: ThemeMode) {
        themeMode = newMode
        Preferences.edit {
            putString(THEME_MODE, newMode.name)
        }
    }
}