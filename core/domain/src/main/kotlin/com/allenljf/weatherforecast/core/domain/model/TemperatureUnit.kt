package com.allenljf.weatherforecast.core.domain.model

import kotlin.math.roundToInt

/**
 * Display unit for temperatures. All domain values are Celsius (what the API
 * returns); conversion happens only at display time.
 */
enum class TemperatureUnit(val symbol: String) {
    CELSIUS("°C"),
    FAHRENHEIT("°F"),
    ;

    /** Converts a Celsius value to this unit and rounds it for display. */
    fun format(celsius: Double): Int = when (this) {
        CELSIUS -> celsius.roundToInt()
        FAHRENHEIT -> (celsius * 9 / 5 + 32).roundToInt()
    }

    companion object {
        val DEFAULT = CELSIUS

        fun fromName(name: String?): TemperatureUnit =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
