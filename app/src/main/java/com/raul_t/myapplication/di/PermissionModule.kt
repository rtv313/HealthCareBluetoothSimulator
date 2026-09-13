package com.raul_t.myapplication.di

import com.raul_t.myapplication.core.util.PermissionChecker
import com.raul_t.myapplication.core.util.PermissionCheckerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PermissionModule {

    @Binds
    @Singleton
    abstract fun bindPermissionChecker(permissionCheckerImpl: PermissionCheckerImpl): PermissionChecker
}
