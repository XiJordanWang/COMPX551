package com.example.wearosassignment.presentation

import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType

/**
 * Receives heart rate in the background, so the tile and the complication keep updating after
 * SensorActivity is closed. Health Services binds to this service and delivers batched readings;
 * unlike MeasureClient this does not keep the sensor running continuously, which is why it is the
 * appropriate client for background surfaces.
 */
class HeartRatePassiveService : PassiveListenerService() {

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        dataPoints.getData(DataType.HEART_RATE_BPM).lastOrNull()?.let {
            HeartRateUpdates.publish(this, it.value)
        }
    }

    override fun onPermissionLost() {
        // The user revoked the heart rate permission; stop asking Health Services for data.
        HeartRateMonitor.stop(this)
    }
}
