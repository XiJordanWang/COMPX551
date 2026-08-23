package com.example.wearosassignment.presentation

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

/** Plausible bounds for a wrist heart rate reading, used to scale the RANGED_VALUE arc. */
private const val MIN_BPM = 40f
private const val MAX_BPM = 200f

/**
 * Surfaces the latest heart rate on a watch face.
 *
 * The watch face refreshes this on its own schedule (UPDATE_PERIOD_SECONDS in the manifest), but
 * HeartRateUpdates also calls requestUpdateAll() whenever a new reading arrives, so the value on
 * the watch face tracks live data instead of waiting for the next period.
 */
class HeartRateComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? = complicationFor(type, 72.0)

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val bpm = HeartRateStore.read(this)?.bpm ?: return placeholderFor(request.complicationType)
        Log.d("HeartRateComplication", "onComplicationRequest id=${request.complicationInstanceId} bpm=$bpm")
        return complicationFor(request.complicationType, bpm)
    }

    private fun complicationFor(type: ComplicationType, bpm: Double): ComplicationData? {
        val description = PlainComplicationText.Builder("Heart rate ${bpm.toInt()} bpm").build()
        return when (type) {
            ComplicationType.SHORT_TEXT ->
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("${bpm.toInt()}").build(),
                    contentDescription = description
                )
                    .setTitle(PlainComplicationText.Builder("bpm").build())
                    .setTapAction(tapAction())
                    .build()

            ComplicationType.RANGED_VALUE ->
                RangedValueComplicationData.Builder(
                    value = bpm.toFloat().coerceIn(MIN_BPM, MAX_BPM),
                    min = MIN_BPM,
                    max = MAX_BPM,
                    contentDescription = description
                )
                    .setText(PlainComplicationText.Builder("${bpm.toInt()}").build())
                    .setTapAction(tapAction())
                    .build()

            else -> null
        }
    }

    /** Shown before the first reading exists, so the slot is not simply blank. */
    private fun placeholderFor(type: ComplicationType): ComplicationData? {
        val description = PlainComplicationText.Builder("No heart rate reading yet").build()
        return when (type) {
            ComplicationType.SHORT_TEXT ->
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("--").build(),
                    contentDescription = description
                )
                    .setTitle(PlainComplicationText.Builder("bpm").build())
                    .setTapAction(tapAction())
                    .build()

            ComplicationType.RANGED_VALUE ->
                RangedValueComplicationData.Builder(
                    value = MIN_BPM,
                    min = MIN_BPM,
                    max = MAX_BPM,
                    contentDescription = description
                )
                    .setText(PlainComplicationText.Builder("--").build())
                    .setTapAction(tapAction())
                    .build()

            else -> null
        }
    }

    /** Tapping the complication opens the sensor screen. */
    private fun tapAction(): PendingIntent =
        PendingIntent.getActivity(
            this,
            0,
            Intent(this, SensorActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
}
