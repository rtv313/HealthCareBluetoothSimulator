package com.raul_t.myapplication.di

import com.raul_t.myapplication.ble.BleManager
import com.raul_t.myapplication.ble.BleManagerImpl
import com.raul_t.myapplication.ble_connect.BleScannerManager
import com.raul_t.myapplication.ble_connect.BleScannerManagerImpl
import com.raul_t.myapplication.data.repository.BleConnectionRepositoryImpl
import com.raul_t.myapplication.domain.repository.BleConnectionRepository
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

    @Binds
    @Singleton
    abstract fun bindBleScannerManager(bleScannerManagerImpl: BleScannerManagerImpl): BleScannerManager

    @Binds
    @Singleton
    abstract fun bindBleConnectionRepository(bleConnectionRepositoryImpl: BleConnectionRepositoryImpl): BleConnectionRepository
}
