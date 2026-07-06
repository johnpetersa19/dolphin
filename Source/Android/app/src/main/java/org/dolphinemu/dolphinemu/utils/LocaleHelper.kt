// SPDX-License-Identifier: GPL-2.0-or-later

package org.dolphinemu.dolphinemu.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {
    private const val LANGUAGE_PREF_KEY = "pref_app_language"
    
    // Language codes
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_GERMAN = "de"
    const val LANGUAGE_FRENCH = "fr"
    const val LANGUAGE_SPANISH = "es"
    const val LANGUAGE_ITALIAN = "it"
    const val LANGUAGE_DUTCH = "nl"
    const val LANGUAGE_JAPANESE = "ja"
    const val LANGUAGE_SIMPLIFIED_CHINESE = "zh"
    const val LANGUAGE_TRADITIONAL_CHINESE = "zh_TW"
    const val LANGUAGE_KOREAN = "ko"
    const val LANGUAGE_SYSTEM = "system" // Use system language
    
    /**
     * Set the app language
     */
    fun setLanguage(context: Context, languageCode: String) {
        val preferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        preferences.edit().putString(LANGUAGE_PREF_KEY, languageCode).apply()
        applyLanguage(context, languageCode)
    }
    
    /**
     * Get the current app language preference
     */
    fun getLanguage(context: Context): String {
        val preferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        return preferences.getString(LANGUAGE_PREF_KEY, LANGUAGE_SYSTEM) ?: LANGUAGE_SYSTEM
    }
    
    /**
     * Apply the selected language to the context
     */
    fun applyLanguage(context: Context, languageCode: String) {
        val locale = when (languageCode) {
            LANGUAGE_SYSTEM -> Locale.getDefault()
            LANGUAGE_TRADITIONAL_CHINESE -> Locale("zh", "TW")
            else -> Locale(languageCode)
        }
        
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
    }
    
    /**
     * Wrap context with the selected language
     */
    fun attachBaseContext(context: Context): Context {
        val languageCode = getLanguage(context)
        return if (languageCode != LANGUAGE_SYSTEM) {
            val locale = when (languageCode) {
                LANGUAGE_TRADITIONAL_CHINESE -> Locale("zh", "TW")
                else -> Locale(languageCode)
            }
            
            val config = Configuration(context.resources.configuration)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocale(locale)
                context.createConfigurationContext(config)
            } else {
                @Suppress("DEPRECATION")
                config.locale = locale
                context.createConfigurationContext(config)
            }
        } else {
            context
        }
    }
}
