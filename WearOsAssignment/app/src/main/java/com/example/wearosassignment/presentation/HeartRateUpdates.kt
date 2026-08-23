package com.example.wearosassignment.presentation

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.wear.tiles.TileService
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester

/**
 * Stores a reading and pushes it to both glanceable surfaces.
 *
 * Neither surface polls us: a tile is only re-rendered when the system asks, and a complication
 * is only refreshed on its update period. Requesting an update here is what makes them live
 * rather than merely periodic.
 */
object HeartRateUpdates {

    fun publish(context: Context, bpm: Double) {
        HeartRateStore.save(context, bpm)
        notifySurfaces(context)
    }

    fun notifySurfaces(context: Context) {
        val appContext = context.applicationContext

        TileService.getUpdater(appContext)
            .requestUpdate(HeartRateTileService::class.java)

        ComplicationDataSourceUpdateRequester
            .create(
                context = appContext,
                complicationDataSourceComponent = ComponentName(
                    appContext,
                    HeartRateComplicationService::class.java
                )
            )
            .requestUpdateAll()

        Log.d("HeartRateUpdates", "requested tile + complication refresh")
    }
}
