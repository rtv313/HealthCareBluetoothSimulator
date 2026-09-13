package com.raul_t.myapplication.presentation.common

import android.content.Context
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.raul_t.myapplication.core.util.PermissionUtils

/**
 * A state object that manages the permission flow for simulation services.
 * Following SOLID principles, this state only handles the permission lifecycle,
 * leaving the responsibility of executing actions to the UI layer.
 */
@Stable
class SimulationPermissionState(
    private val context: Context,
    private val launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
) {
    /**
     * Internal callback to communicate the result of a request back to the caller.
     */
    private var onResult: ((Boolean) -> Unit)? = null

    /**
     * Whether all required permissions are currently granted.
     */
    var allGranted by mutableStateOf(PermissionUtils.hasSimulationPermissions(context))
        private set

    /**
     * Handles the result from the activity launcher.
     */
    fun onPermissionResult(results: Map<String, Boolean>) {
        val success = results.values.all { it }
        allGranted = PermissionUtils.hasSimulationPermissions(context)
        onResult?.invoke(success)
        onResult = null
    }

    /**
     * Requests the required permissions.
     * @param onResult Optional callback that is invoked with the result of this specific request.
     */
    fun requestPermissions(onResult: ((Boolean) -> Unit)? = null) {
        this.onResult = onResult
        val permissions = PermissionUtils.getSimulationPermissions()
        if (permissions.isNotEmpty()) {
            launcher.launch(permissions.toTypedArray())
        } else {
            // No runtime permissions needed for this SDK version
            allGranted = true
            onResult?.invoke(true)
        }
    }
}

/**
 * Creates and remembers a [SimulationPermissionState].
 */
@Composable
fun rememberSimulationPermissionState(): SimulationPermissionState {
    val context = LocalContext.current
    
    // We need a stable reference to the state to avoid losing the callback during recomposition
    val state = remember { mutableStateOf<SimulationPermissionState?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        state.value?.onPermissionResult(results)
    }

    return remember(context, launcher) {
        SimulationPermissionState(context, launcher).also {
            state.value = it
        }
    }
}
