package com.raul_t.myapplication.di

import com.raul_t.myapplication.data.repository.SensorRepositoryImpl
import com.raul_t.myapplication.domain.repository.SensorRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SensorModule {
    @Binds
    abstract fun bindSensorRepository(repository: SensorRepositoryImpl): SensorRepository
}