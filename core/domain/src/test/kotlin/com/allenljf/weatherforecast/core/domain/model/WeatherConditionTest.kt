package com.allenljf.weatherforecast.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherConditionTest {

    @Test
    fun `maps clear sky codes`() {
        assertEquals(WeatherCondition.CLEAR, WeatherCondition.fromWmoCode(0))
        assertEquals(WeatherCondition.MAINLY_CLEAR, WeatherCondition.fromWmoCode(1))
    }

    @Test
    fun `maps cloud codes`() {
        assertEquals(WeatherCondition.PARTLY_CLOUDY, WeatherCondition.fromWmoCode(2))
        assertEquals(WeatherCondition.OVERCAST, WeatherCondition.fromWmoCode(3))
    }

    @Test
    fun `maps fog codes`() {
        assertEquals(WeatherCondition.FOG, WeatherCondition.fromWmoCode(45))
        assertEquals(WeatherCondition.FOG, WeatherCondition.fromWmoCode(48))
    }

    @Test
    fun `maps drizzle codes`() {
        assertEquals(WeatherCondition.DRIZZLE, WeatherCondition.fromWmoCode(51))
        assertEquals(WeatherCondition.DRIZZLE, WeatherCondition.fromWmoCode(53))
        assertEquals(WeatherCondition.DRIZZLE, WeatherCondition.fromWmoCode(55))
        assertEquals(WeatherCondition.FREEZING_DRIZZLE, WeatherCondition.fromWmoCode(56))
        assertEquals(WeatherCondition.FREEZING_DRIZZLE, WeatherCondition.fromWmoCode(57))
    }

    @Test
    fun `maps rain codes`() {
        assertEquals(WeatherCondition.RAIN, WeatherCondition.fromWmoCode(61))
        assertEquals(WeatherCondition.RAIN, WeatherCondition.fromWmoCode(63))
        assertEquals(WeatherCondition.RAIN, WeatherCondition.fromWmoCode(65))
        assertEquals(WeatherCondition.FREEZING_RAIN, WeatherCondition.fromWmoCode(66))
        assertEquals(WeatherCondition.FREEZING_RAIN, WeatherCondition.fromWmoCode(67))
    }

    @Test
    fun `maps snow codes`() {
        assertEquals(WeatherCondition.SNOW, WeatherCondition.fromWmoCode(71))
        assertEquals(WeatherCondition.SNOW, WeatherCondition.fromWmoCode(73))
        assertEquals(WeatherCondition.SNOW, WeatherCondition.fromWmoCode(75))
        assertEquals(WeatherCondition.SNOW_GRAINS, WeatherCondition.fromWmoCode(77))
    }

    @Test
    fun `maps shower codes`() {
        assertEquals(WeatherCondition.RAIN_SHOWERS, WeatherCondition.fromWmoCode(80))
        assertEquals(WeatherCondition.RAIN_SHOWERS, WeatherCondition.fromWmoCode(81))
        assertEquals(WeatherCondition.RAIN_SHOWERS, WeatherCondition.fromWmoCode(82))
        assertEquals(WeatherCondition.SNOW_SHOWERS, WeatherCondition.fromWmoCode(85))
        assertEquals(WeatherCondition.SNOW_SHOWERS, WeatherCondition.fromWmoCode(86))
    }

    @Test
    fun `maps thunderstorm codes`() {
        assertEquals(WeatherCondition.THUNDERSTORM, WeatherCondition.fromWmoCode(95))
        assertEquals(WeatherCondition.THUNDERSTORM_WITH_HAIL, WeatherCondition.fromWmoCode(96))
        assertEquals(WeatherCondition.THUNDERSTORM_WITH_HAIL, WeatherCondition.fromWmoCode(99))
    }

    @Test
    fun `maps unrecognised codes to UNKNOWN`() {
        assertEquals(WeatherCondition.UNKNOWN, WeatherCondition.fromWmoCode(42))
        assertEquals(WeatherCondition.UNKNOWN, WeatherCondition.fromWmoCode(-1))
        assertEquals(WeatherCondition.UNKNOWN, WeatherCondition.fromWmoCode(100))
    }
}
