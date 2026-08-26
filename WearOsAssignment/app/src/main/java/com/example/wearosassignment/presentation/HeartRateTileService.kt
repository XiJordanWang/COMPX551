package com.example.wearosassignment.presentation

import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.protolayout.TypeBuilders.StringLayoutConstraint
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicFloat
import androidx.wear.protolayout.expression.PlatformHealthSources
import androidx.wear.protolayout.material3.Typography
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.types.LayoutString
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.TileBuilders.Tile
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.TimeUnit

private const val RESOURCES_VERSION = "1"

/**
 * A tile showing the most recent heart rate recorded by SensorActivity.
 *
 * Tiles are not continuously running: the system calls onTileRequest when the tile scrolls into
 * view and again every freshnessIntervalMillis. So instead of measuring here, the tile renders
 * whatever SensorActivity last wrote to HeartRateStore.
 */
class HeartRateTileService : TileService() {

    // For Layout
    override fun onTileRequest(requestParams: TileRequest): ListenableFuture<Tile> {
        val reading = HeartRateStore.read(this)

        val layout = materialScope(
            context = this,
            deviceConfiguration = requestParams.deviceConfiguration
        ) {
            primaryLayout(
                // The titleSlot, typically for a primary title or header.
                titleSlot = {
                    text(
                        text = LayoutString("Heart Rate"),
                        typography = Typography.LABEL_SMALL
                    )
                },
                // The mainSlot, for the core content.
                mainSlot = {
                    text(
                        text = liveHeartRate(reading),
                        typography = Typography.NUMERAL_EXTRA_LARGE
                    )
                },
                // The bottomSlot, often used for actions or supplemental information. This is also where an edge button appears.
                bottomSlot = {
                    text(
                        text = LayoutString(subtitle(reading)),
                        typography = Typography.BODY_SMALL
                    )
                }
            )
        }

        val tile = Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            // Ask the system to re-request the layout periodically, so a stale reading
            // does not sit on screen indefinitely.
            .setFreshnessIntervalMillis(TimeUnit.MINUTES.toMillis(1)) // ？
            .setTileTimeline(Timeline.fromLayoutElement(layout))
            .build()

        return Futures.immediateFuture(tile)
    }

    // For pictures
    override fun onTileResourcesRequest(
        requestParams: ResourcesRequest
    ): ListenableFuture<Resources> =
        Futures.immediateFuture(
            Resources.Builder().setVersion(RESOURCES_VERSION).build()
        )

    /**
     * The number the tile shows.
     *
     * The static half is only a fallback, rendered when the platform source is unavailable - for
     * instance when the heart rate permission has not been granted. The dynamic half binds to the
     * platform heart rate stream, which the renderer subscribes to directly: it repaints the value
     * on screen without going back through onTileRequest, which is what makes the tile update
     * while the user is looking at it.
     */
    private fun liveHeartRate(reading: HeartRateStore.Reading?): LayoutString {
        val fallback = reading?.let { "${it.bpm.toInt()}" } ?: "--"
        val live = PlatformHealthSources.heartRateBpm()
            .format(DynamicFloat.FloatFormatter.Builder().setMaxFractionDigits(0).build())
        return LayoutString(
            fallback,
            live,
            // Reserves enough width for a three digit reading so the layout does not reflow.
            StringLayoutConstraint.Builder("888").build()
        )
    }

    /**
     * The main number is bound to the live platform stream, so a "measured N minutes ago" caption
     * would describe the fallback rather than what is on screen.
     */
    private fun subtitle(reading: HeartRateStore.Reading?): String =
        if (reading == null) "Open the app to measure" else "bpm"
}
