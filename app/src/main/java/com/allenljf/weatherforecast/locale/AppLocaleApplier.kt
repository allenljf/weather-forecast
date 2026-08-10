package com.allenljf.weatherforecast.locale

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.allenljf.weatherforecast.core.domain.model.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Applies the stored language as the app's locale.
 *
 * On API 33+ this goes straight to the platform [LocaleManager] — the system
 * then persists the choice and recreates activities itself. Below 33 the
 * AppCompat backport handles it.
 */
@Singleton
class AppLocaleApplier @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun currentTag(): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java)
                ?.applicationLocales
                ?.takeIf { !it.isEmpty }
                ?.get(0)
                ?.toLanguageTag()
        } else {
            AppCompatDelegate.getApplicationLocales()
                .toLanguageTags()
                .takeIf { it.isNotEmpty() }
                ?.substringBefore(',')
        }

    fun apply(language: AppLanguage) {
        if (currentTag().orEmpty().equals(language.tag, ignoreCase = true)) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java)
                ?.applicationLocales = LocaleList.forLanguageTags(language.tag)
        } else {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(language.tag),
            )
        }
    }
}
