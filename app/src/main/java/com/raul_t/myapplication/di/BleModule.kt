package com.raul_t.myapplication.di

import com.raul_t.myapplication.ble.BleManager
import com.raul_t.myapplication.ble.BleManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BleModule {

    @Binds
    @Singleton
    abstract fun bindBleManager(bleManagerImpl: BleManagerImpl): BleManager
}
