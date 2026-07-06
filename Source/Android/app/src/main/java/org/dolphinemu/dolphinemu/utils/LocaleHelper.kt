// SPDX-License-Identifier: GPL-2.0-or-later

package org.dolphinemu.dolphinemu.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.preference.PreferenceManager
import java.util.*

object LocaleHelper {
    // Language codes
    const val LANGUAGE_SYSTEM = "system"
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_PORTUGUESE_BR = "pt_BR"
    const val LANGUAGE_PORTUGUESE_PT = "pt"
    const val LANGUAGE_SPANISH = "es"
    const val LANGUAGE_FRENCH = "fr"
    const val LANGUAGE_GERMAN = "de"
    const val LANGUAGE_ITALIAN = "it"
    const val LANGUAGE_DUTCH = "nl"
    const val LANGUAGE_JAPANESE = "ja"
    const val LANGUAGE_SIMPLIFIED_CHINESE = "zh"
    const val LANGUAGE_TRADITIONAL_CHINESE = "zh_TW"
    const val LANGUAGE_KOREAN = "ko"
    const val LANGUAGE_RUSSIAN = "ru"
    const val LANGUAGE_TURKISH = "tr"

    private const val LANGUAGE_PREF_KEY = "pref_app_language"

    /**
     * Get the current app language preference
     */
    fun getLanguage(context: Context): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(LANGUAGE_PREF_KEY, LANGUAGE_SYSTEM) ?: LANGUAGE_SYSTEM
    }

    /**
     * Set the app language
     */
    fun setLanguage(context: Context, languageCode: String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().putString(LANGUAGE_PREF_KEY, languageCode).apply()
        applyLanguage(context, languageCode)
    }

    /**
     * Apply the selected language to the context
     */
    fun applyLanguage(context: Context, languageCode: String) {
        val locale = getLocaleFromCode(languageCode)
        
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    /**
     * Wrap context with the selected language
     */
    fun attachBaseContext(context: Context): Context {
        val languageCode = getLanguage(context)
        return if (languageCode != LANGUAGE_SYSTEM) {
            val locale = getLocaleFromCode(languageCode)
            val config = Configuration(context.resources.configuration)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocale(locale)
            } else {
                @Suppress("DEPRECATION")
                config.locale = locale
            }
            
            context.createConfigurationContext(config)
        } else {
            context
        }
    }

    /**
     * Get Locale object from language code
     */
    private fun getLocaleFromCode(languageCode: String): Locale {
        return when (languageCode) {
            LANGUAGE_PORTUGUESE_BR -> Locale("pt", "BR")
            LANGUAGE_PORTUGUESE_PT -> Locale("pt", "PT")
            LANGUAGE_TRADITIONAL_CHINESE -> Locale("zh", "TW")
            LANGUAGE_SYSTEM -> Locale.getDefault()
            else -> Locale(languageCode)
        }
    }

    /**
     * Get all supported languages
     */
    fun getSupportedLanguages(): Map<String, String> {
        return mapOf(
            LANGUAGE_SYSTEM to "System",
            LANGUAGE_ENGLISH to "English",
            LANGUAGE_PORTUGUESE_BR to "Português (Brasil)",
            LANGUAGE_PORTUGUESE_PT to "Português",
            LANGUAGE_SPANISH to "Español",
            LANGUAGE_FRENCH to "Français",
            LANGUAGE_GERMAN to "Deutsch",
            LANGUAGE_ITALIAN to "Italiano",
            LANGUAGE_DUTCH to "Nederlands",
            LANGUAGE_JAPANESE to "日本語",
            LANGUAGE_SIMPLIFIED_CHINESE to "简体中文",
            LANGUAGE_TRADITIONAL_CHINESE to "繁體中文",
            LANGUAGE_KOREAN to "한국어",
            LANGUAGE_RUSSIAN to "Русский",
            LANGUAGE_TURKISH to "Türkçe"
        )
    }
}
