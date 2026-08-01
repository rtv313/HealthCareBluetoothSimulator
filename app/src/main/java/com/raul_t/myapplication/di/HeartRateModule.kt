package com.raul_t.myapplication.di

import com.raul_t.myapplication.data.repository.HeartRateRepositoryImpl
import com.raul_t.myapplication.domain.repository.HeartRateRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class HeartRateModule {

    @Binds
    abstract fun bindHeartRateRepository(repository: HeartRateRepositoryImpl): HeartRateRepository
}