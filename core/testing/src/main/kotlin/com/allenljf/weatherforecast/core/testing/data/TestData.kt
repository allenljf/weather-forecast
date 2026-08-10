package com.allenljf.weatherforecast.core.testing.data

import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.model.CurrentWeather
import com.allenljf.weatherforecast.core.domain.model.DailyForecast
import com.allenljf.weatherforecast.core.domain.model.HourlyForecast
import com.allenljf.weatherforecast.core.domain.model.WeatherCondition
import com.allenljf.weatherforecast.core.domain.model.WeatherForecast
import java.time.LocalDate
import java.time.LocalDateTime

object TestData {

    val taipei = City(id = 1668341, name = "Taipei", country = "Taiwan", latitude = 25.0478, longitude = 121.5319)
    val tokyo = City(id = 1850147, name = "Tokyo", country = "Japan", latitude = 35.6895, longitude = 139.6917)
    val london = City(id = 2643743, name = "London", country = "United Kingdom", latitude = 51.5085, longitude = -0.1257)

    fun forecast(
        temperature: Double = 28.5,
        condition: WeatherCondition = WeatherCondition.PARTLY_CLOUDY,
    ): WeatherForecast {
        val date = LocalDate.of(2026, 8, 10)
        return WeatherForecast(
            current = CurrentWeather(
                temperature = temperature,
                feelsLike = temperature + 2.0,
                humidity = 68,
                windSpeed = 11.5,
                condition = condition,
                time = LocalDateTime.of(2026, 8, 10, 12, 0),
            ),
            hourly = (0 until 24).map { hour ->
                HourlyForecast(
                    time = LocalDateTime.of(2026, 8, 10, hour, 0),
                    temperature = temperature - 3 + hour * 0.25,
                    condition = condition,
                    precipitationProbability = 10 + hour,
                )
            },
            daily = (0 until 7).map { day ->
                DailyForecast(
                    date = date.plusDays(day.toLong()),
                    minTemperature = temperature - 5,
                    maxTemperature = temperature + 3,
                    condition = condition,
                    precipitationProbability = 40,
                    sunrise = LocalDateTime.of(2026, 8, 10, 5, 16).plusDays(day.toLong()),
                    sunset = LocalDateTime.of(2026, 8, 10, 18, 35).plusDays(day.toLong()),
                )
            },
        )
    }
}
