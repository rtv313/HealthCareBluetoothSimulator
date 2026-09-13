package com.raul_t.myapplication.core.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface PermissionChecker {
    fun hasSimulationPermissions(): Boolean
}

@Singleton
class PermissionCheckerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PermissionChecker {
    override fun hasSimulationPermissions(): Boolean {
        return PermissionUtils.hasSimulationPermissions(context)
    }
}
