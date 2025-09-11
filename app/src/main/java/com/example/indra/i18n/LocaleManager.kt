package com.example.indra.i18n

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleManager {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_LOCALE = "app_locale"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSavedLocale(context: Context): String? = prefs(context).getString(KEY_LOCALE, null)

    fun setLocale(context: Context, languageTag: String) {
        prefs(context).edit().putString(KEY_LOCALE, languageTag).apply()
    }

    fun wrapContext(context: Context): ContextWrapper {
        val lang = getSavedLocale(context) ?: return ContextWrapper(context)
        val locale = Locale.forLanguageTag(lang)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            config.setLocale(locale)
            return ContextWrapper(context.createConfigurationContext(config))
        } else {
            @Suppress("DEPRECATION")
            config.setLocale(locale)
            @Suppress("DEPRECATION")
            val newContext = context.createConfigurationContext(config)
            return ContextWrapper(newContext)
        }
    }

    fun applyAndRestart(activity: Activity, languageTag: String) {
        setLocale(activity, languageTag)
        activity.recreate()
    }
}


