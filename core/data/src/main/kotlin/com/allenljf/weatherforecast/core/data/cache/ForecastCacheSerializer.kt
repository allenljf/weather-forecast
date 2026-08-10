package com.allenljf.weatherforecast.core.data.cache

import com.allenljf.weatherforecast.core.domain.model.DailyForecast
import com.allenljf.weatherforecast.core.domain.model.HourlyForecast
import com.allenljf.weatherforecast.core.domain.model.WeatherCondition
import com.allenljf.weatherforecast.core.domain.model.CurrentWeather
import com.allenljf.weatherforecast.core.domain.model.WeatherForecast
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Converts a [WeatherForecast] to and from the JSON stored in the cache table.
 * Domain models stay free of serialization annotations; these mirrors carry them.
 *
 * [decode] returns null for anything unreadable (corrupt row, older schema),
 * so a bad cache entry degrades to "no cache" instead of crashing.
 */
class ForecastCacheSerializer @Inject constructor() {

    private val json = Json { ignoreUnknownKeys = true }

    fun encode(forecast: WeatherForecast): String = json.encodeToString(forecast.toCacheModel())

    fun decode(payload: String): WeatherForecast? = runCatching {
        json.decodeFromString<CachedForecastPayload>(payload).toDomain()
    }.getOrNull()
}

@Serializable
internal data class CachedForecastPayload(
    val current: CurrentPayload,
    val hourly: List<HourlyPayload>,
    val daily: List<DailyPayload>,
)

@Serializable
internal data class CurrentPayload(
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val conditionCode: String,
    val time: String,
)

@Serializable
internal data class HourlyPayload(
    val time: String,
    val temperature: Double,
    val conditionCode: String,
    val precipitationProbability: Int? = null,
)

@Serializable
internal data class DailyPayload(
    val date: String,
    val minTemperature: Double,
    val maxTemperature: Double,
    val conditionCode: String,
    val precipitationProbability: Int? = null,
    val sunrise: String? = null,
    val sunset: String? = null,
)

private fun WeatherForecast.toCacheModel() = CachedForecastPayload(
    current = CurrentPayload(
        temperature = current.temperature,
        feelsLike = current.feelsLike,
        humidity = current.humidity,
        windSpeed = current.windSpeed,
        conditionCode = current.condition.name,
        time = current.time.toString(),
    ),
    hourly = hourly.map {
        HourlyPayload(
            time = it.time.toString(),
            temperature = it.temperature,
            conditionCode = it.condition.name,
            precipitationProbability = it.precipitationProbability,
        )
    },
    daily = daily.map {
        DailyPayload(
            date = it.date.toString(),
            minTemperature = it.minTemperature,
            maxTemperature = it.maxTemperature,
            conditionCode = it.condition.name,
            precipitationProbability = it.precipitationProbability,
            sunrise = it.sunrise?.toString(),
            sunset = it.sunset?.toString(),
        )
    },
)

private fun CachedForecastPayload.toDomain() = WeatherForecast(
    current = CurrentWeather(
        temperature = current.temperature,
        feelsLike = current.feelsLike,
        humidity = current.humidity,
        windSpeed = current.windSpeed,
        condition = current.conditionCode.toCondition(),
        time = LocalDateTime.parse(current.time),
    ),
    hourly = hourly.map {
        HourlyForecast(
            time = LocalDateTime.parse(it.time),
            temperature = it.temperature,
            condition = it.conditionCode.toCondition(),
            precipitationProbability = it.precipitationProbability,
        )
    },
    daily = daily.map {
        DailyForecast(
            date = LocalDate.parse(it.date),
            minTemperature = it.minTemperature,
            maxTemperature = it.maxTemperature,
            condition = it.conditionCode.toCondition(),
            precipitationProbability = it.precipitationProbability,
            sunrise = it.sunrise?.let(LocalDateTime::parse),
            sunset = it.sunset?.let(LocalDateTime::parse),
        )
    },
)

private fun String.toCondition(): WeatherCondition =
    WeatherCondition.entries.firstOrNull { it.name == this } ?: WeatherCondition.UNKNOWN
