package com.allenljf.weatherforecast.core.testing.network

import com.allenljf.weatherforecast.core.common.network.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNetworkMonitor(initiallyOnline: Boolean = true) : NetworkMonitor {
    val onlineState = MutableStateFlow(initiallyOnline)
    override val isOnline: Flow<Boolean> = onlineState
}
