package com.allenljf.weatherforecast.core.network.di

import com.allenljf.weatherforecast.core.common.network.NetworkMonitor
import com.allenljf.weatherforecast.core.network.monitor.ConnectivityNetworkMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface NetworkMonitorModule {

    @Binds
    @Singleton
    fun bindNetworkMonitor(impl: ConnectivityNetworkMonitor): NetworkMonitor
}
