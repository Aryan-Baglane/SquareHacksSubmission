package com.example.indra.ui.theme

import android.content.Context
import android.content.SharedPreferences

object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"
    
    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    
    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    fun getThemeMode(context: Context): String =
        prefs(context).getString(KEY_THEME_MODE, THEME_SYSTEM) ?: THEME_SYSTEM
    
    fun setThemeMode(context: Context, themeMode: String) {
        prefs(context).edit().putString(KEY_THEME_MODE, themeMode).apply()
    }
    
    fun isDarkTheme(context: Context, isSystemInDarkTheme: Boolean): Boolean {
        return when (getThemeMode(context)) {
            THEME_DARK -> true
            THEME_LIGHT -> false
            else -> isSystemInDarkTheme
        }
    }
}
