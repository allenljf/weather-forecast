package com.allenljf.weatherforecast.core.designsystem.component

import androidx.annotation.StringRes
import com.allenljf.weatherforecast.core.designsystem.R
import com.allenljf.weatherforecast.core.domain.model.AppLanguage

@get:StringRes
val AppLanguage.labelRes: Int
    get() = when (this) {
        AppLanguage.ENGLISH -> R.string.language_english
        AppLanguage.TRADITIONAL_CHINESE -> R.string.language_traditional_chinese
    }
