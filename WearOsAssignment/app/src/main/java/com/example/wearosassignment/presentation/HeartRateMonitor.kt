package com.example.wearosassignment.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig

/** Registers and clears the background heart rate listener. */
object HeartRateMonitor {

    private const val TAG = "HeartRateMonitor"

    /** From API 36 the heart rate sensor is guarded by READ_HEART_RATE instead of BODY_SENSORS. */
    val PERMISSION: String =
        if (Build.VERSION.SDK_INT >= 36) "android.permission.health.READ_HEART_RATE"
        else Manifest.permission.BODY_SENSORS

    fun isPermitted(context: Context) =
        context.checkSelfPermission(PERMISSION) == PackageManager.PERMISSION_GRANTED

    fun start(context: Context) {
        // Registering without the permission would only fail inside Health Services.
        if (!isPermitted(context)) {
            Log.d(TAG, "Heart rate permission not granted; not registering passive listener")
            return
        }

        val config = PassiveListenerConfig.Builder()
            .setDataTypes(setOf(DataType.HEART_RATE_BPM))
            .build()

        HealthServices.getClient(context).passiveMonitoringClient
            .setPassiveListenerServiceAsync(HeartRatePassiveService::class.java, config)
            .addListener(
                { Log.d(TAG, "Passive heart rate listener registered") },
                context.mainExecutor
            )
    }

    fun stop(context: Context) {
        HealthServices.getClient(context).passiveMonitoringClient
            .clearPassiveListenerServiceAsync()
    }
}
