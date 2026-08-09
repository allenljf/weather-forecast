package com.allenljf.weatherforecast.feature.cities

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.allenljf.weatherforecast.core.designsystem.component.EmptyState
import com.allenljf.weatherforecast.core.domain.model.City

@Composable
fun CitiesRoute(
    onBackClick: () -> Unit,
    onCitySelected: () -> Unit,
    viewModel: CitiesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CitiesScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onClearSearch = viewModel::onClearSearch,
        onAddCity = { city ->
            viewModel.onAddCity(city)
            onCitySelected()
        },
        onRemoveCity = viewModel::onRemoveCity,
        onSelectCity = { cityId ->
            viewModel.onSelectCity(cityId)
            onCitySelected()
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitiesScreen(
    uiState: CitiesUiState,
    onBackClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onAddCity: (City) -> Unit,
    onRemoveCity: (Long) -> Unit,
    onSelectCity: (Long) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Cities") },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_input"),
                placeholder = { Text(text = "Search city to add") },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = onClearSearch,
                            modifier = Modifier.testTag("clear_search_button"),
                        ) {
                            Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
            )

            if (uiState.isInSearchMode) {
                SearchResults(
                    uiState = uiState,
                    onAddCity = onAddCity,
                )
            } else {
                SavedCities(
                    uiState = uiState,
                    onRemoveCity = onRemoveCity,
                    onSelectCity = onSelectCity,
                )
            }
        }
    }
}

@Composable
private fun SearchResults(
    uiState: CitiesUiState,
    onAddCity: (City) -> Unit,
) {
    when {
        uiState.isSearching -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.testTag("search_loading"))
            }
        }

        uiState.searchFailed -> {
            EmptyState(message = "Search failed. Check your connection and try again.")
        }

        uiState.searchResults.isEmpty() -> {
            EmptyState(message = "No matching cities. Keep typing to search.")
        }

        else -> {
            LazyColumn(modifier = Modifier.testTag("search_results_list")) {
                items(uiState.searchResults, key = { it.id }) { city ->
                    ListItem(
                        modifier = Modifier.testTag("search_result_${city.id}"),
                        headlineContent = { Text(text = city.name) },
                        supportingContent = { Text(text = city.country) },
                        trailingContent = {
                            val alreadySaved = uiState.savedCities.any { it.id == city.id }
                            if (alreadySaved) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Already added",
                                )
                            } else {
                                IconButton(
                                    onClick = { onAddCity(city) },
                                    modifier = Modifier.testTag("add_city_${city.id}"),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "Add ${city.name}",
                                    )
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedCities(
    uiState: CitiesUiState,
    onRemoveCity: (Long) -> Unit,
    onSelectCity: (Long) -> Unit,
) {
    if (uiState.savedCities.isEmpty()) {
        EmptyState(message = "No saved cities. Search above to add one.")
        return
    }

    LazyColumn(modifier = Modifier.testTag("saved_cities_list")) {
        items(uiState.savedCities, key = { it.id }) { city ->
            val selected = city.id == uiState.selectedCityId
            ListItem(
                modifier = Modifier
                    .clickable { onSelectCity(city.id) }
                    .testTag("saved_city_${city.id}"),
                headlineContent = {
                    Text(
                        text = city.name,
                        style = if (selected) {
                            MaterialTheme.typography.titleMedium
                        } else {
                            MaterialTheme.typography.bodyLarge
                        },
                    )
                },
                supportingContent = { Text(text = city.country) },
                leadingContent = {
                    IconButton(
                        onClick = { onSelectCity(city.id) },
                        modifier = Modifier.testTag("select_city_${city.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = if (selected) "Selected" else "Select ${city.name}",
                            tint = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                        )
                    }
                },
                trailingContent = {
                    IconButton(
                        onClick = { onRemoveCity(city.id) },
                        modifier = Modifier.testTag("delete_city_${city.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete ${city.name}",
                        )
                    }
                },
            )
        }
    }
}
