package com.allenljf.weatherforecast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.allenljf.weatherforecast.core.designsystem.theme.WeatherForecastTheme
import com.allenljf.weatherforecast.navigation.WeatherNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherForecastTheme {
                WeatherNavHost()
            }
        }
    }
}
