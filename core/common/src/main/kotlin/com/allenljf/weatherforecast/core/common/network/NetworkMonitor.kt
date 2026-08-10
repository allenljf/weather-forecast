package com.allenljf.weatherforecast.core.common.network

import kotlinx.coroutines.flow.Flow

/**
 * Reports device connectivity. Implemented with ConnectivityManager in
 * core:network; consumers observe [isOnline] to react to connectivity changes.
 */
interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}
