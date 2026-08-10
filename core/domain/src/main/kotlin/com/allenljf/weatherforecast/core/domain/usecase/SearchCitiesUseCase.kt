package com.allenljf.weatherforecast.core.domain.usecase

import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.repository.CityRepository
import javax.inject.Inject

/**
 * Searches cities by name. Queries shorter than [MIN_QUERY_LENGTH] characters
 * return an empty result without hitting the repository.
 */
class SearchCitiesUseCase @Inject constructor(
    private val cityRepository: CityRepository,
) {
    suspend operator fun invoke(query: String): AppResult<List<City>> {
        val trimmed = query.trim()
        if (trimmed.length < MIN_QUERY_LENGTH) {
            return AppResult.Success(emptyList())
        }
        return cityRepository.searchCities(trimmed)
    }

    companion object {
        const val MIN_QUERY_LENGTH = 2
    }
}
