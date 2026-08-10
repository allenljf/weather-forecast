package com.allenljf.weatherforecast.core.domain.model

/**
 * UI languages the app ships translations for. [tag] is the BCP-47 tag used by
 * the platform per-app locale API; [geocodingCode] is what the Open-Meteo
 * geocoding API accepts for localized place names.
 */
enum class AppLanguage(val tag: String, val geocodingCode: String) {
    ENGLISH("en", "en"),

    /**
     * Traditional Chinese. Open-Meteo returns Latin names for "zh-TW" but
     * localized names for "zh", so the geocoding code is deliberately "zh".
     */
    TRADITIONAL_CHINESE("zh-TW", "zh"),
    ;

    companion object {
        val DEFAULT = ENGLISH

        fun fromTag(tag: String?): AppLanguage =
            entries.firstOrNull { it.tag.equals(tag, ignoreCase = true) } ?: DEFAULT
    }
}
