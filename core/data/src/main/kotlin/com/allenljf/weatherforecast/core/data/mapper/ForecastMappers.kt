package com.allenljf.weatherforecast.core.data.mapper

import com.allenljf.weatherforecast.core.domain.model.CurrentWeather
import com.allenljf.weatherforecast.core.domain.model.DailyForecast
import com.allenljf.weatherforecast.core.domain.model.HourlyForecast
import com.allenljf.weatherforecast.core.domain.model.WeatherCondition
import com.allenljf.weatherforecast.core.domain.model.WeatherForecast
import com.allenljf.weatherforecast.core.network.model.CurrentDto
import com.allenljf.weatherforecast.core.network.model.DailyDto
import com.allenljf.weatherforecast.core.network.model.ForecastResponseDto
import com.allenljf.weatherforecast.core.network.model.HourlyDto
import java.time.LocalDate
import java.time.LocalDateTime

fun ForecastResponseDto.toDomain(): WeatherForecast = WeatherForecast(
    current = current.toDomain(),
    hourly = hourly.toDomain(),
    daily = daily.toDomain(),
)

private fun CurrentDto.toDomain(): CurrentWeather = CurrentWeather(
    temperature = temperature2m,
    feelsLike = apparentTemperature,
    humidity = relativeHumidity2m,
    windSpeed = windSpeed10m,
    condition = WeatherCondition.fromWmoCode(weatherCode),
    time = LocalDateTime.parse(time),
)

private fun HourlyDto.toDomain(): List<HourlyForecast> = time.indices.map { index ->
    HourlyForecast(
        time = LocalDateTime.parse(time[index]),
        temperature = temperature2m[index],
        condition = WeatherCondition.fromWmoCode(weatherCode[index]),
    )
}

private fun DailyDto.toDomain(): List<DailyForecast> = time.indices.map { index ->
    DailyForecast(
        date = LocalDate.parse(time[index]),
        minTemperature = temperature2mMin[index],
        maxTemperature = temperature2mMax[index],
        condition = WeatherCondition.fromWmoCode(weatherCode[index]),
    )
}
