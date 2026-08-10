package com.allenljf.weatherforecast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.allenljf.weatherforecast.core.designsystem.theme.WeatherForecastTheme
import com.allenljf.weatherforecast.core.domain.usecase.ObserveAppLanguageUseCase
import com.allenljf.weatherforecast.locale.AppLocaleApplier
import com.allenljf.weatherforecast.navigation.WeatherNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var observeAppLanguage: ObserveAppLanguageUseCase

    @Inject
    lateinit var localeApplier: AppLocaleApplier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        applyStoredLanguage()
        setContent {
            WeatherForecastTheme {
                WeatherNavHost()
            }
        }
    }

    /**
     * Mirrors the stored language choice onto the platform per-app locale.
     * AppCompat recreates the activity itself when the locale actually changes,
     * so every string resource re-resolves without any manual restart.
     */
    private fun applyStoredLanguage() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                observeAppLanguage()
                    .distinctUntilChanged()
                    .collect(localeApplier::apply)
            }
        }
    }
}
