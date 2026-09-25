package com.example.mezahub.data

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Languages MezaHub ships translations for, each named in its own language. [tag] null means
 * "follow the device". Must stay in sync with res/xml/locales_config.xml and the values-* folders.
 *
 * Filipino and Indonesian use the legacy codes "tl"/"in" — the same names as their resource
 * folders — so a choice made here always resolves to the right strings on every Android version.
 */
enum class AppLanguage(val tag: String?, val nativeName: String?) {
    SYSTEM(null, null),
    ENGLISH("en", "English"),
    JAPANESE("ja", "日本語"),
    KOREAN("ko", "한국어"),
    CHINESE_SIMPLIFIED("zh-CN", "简体中文"),
    CHINESE_TRADITIONAL("zh-TW", "繁體中文"),
    FILIPINO("tl", "Filipino"),
    THAI("th", "ไทย"),
    INDONESIAN("in", "Bahasa Indonesia"),
    MALAY("ms", "Bahasa Melayu"),
    ;

    private fun matches(locale: Locale): Boolean {
        val target = Locale.forLanguageTag(tag ?: return false)
        return legacyCode(target.language) == legacyCode(locale.language) &&
            (target.country.isEmpty() || target.country == locale.country)
    }

    companion object {
        // The system per-app language picker (locales_config) may hand back modern codes.
        private fun legacyCode(language: String): String = when (language) {
            "fil" -> "tl"
            "id" -> "in"
            else -> language
        }

        /** The app-specific language currently applied; SYSTEM when following the device. */
        fun current(): AppLanguage {
            val locale = AppCompatDelegate.getApplicationLocales()[0] ?: return SYSTEM
            return entries.firstOrNull { it.matches(locale) } ?: SYSTEM
        }

        /** Applies [language] app-wide. AppCompat persists it and recreates the activity. */
        fun apply(language: AppLanguage) {
            AppCompatDelegate.setApplicationLocales(
                language.tag?.let { LocaleListCompat.forLanguageTags(it) } ?: LocaleListCompat.getEmptyLocaleList(),
            )
        }
    }
}
