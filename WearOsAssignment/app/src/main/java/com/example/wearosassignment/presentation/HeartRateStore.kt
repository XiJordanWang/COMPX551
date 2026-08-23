package com.example.wearosassignment.presentation

import android.content.Context

/**
 * A tile runs in its own process-less request/response cycle, so it cannot hold a live
 * MeasureCallback the way SensorActivity does. SensorActivity writes the latest reading here and
 * the tile reads it back when the system asks for a layout.
 */
object HeartRateStore {
    private const val PREFS = "heart_rate"
    private const val KEY_BPM = "bpm"
    private const val KEY_UPDATED_AT = "updated_at"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun save(context: Context, bpm: Double) {
        prefs(context).edit()
            .putFloat(KEY_BPM, bpm.toFloat())
            .putLong(KEY_UPDATED_AT, System.currentTimeMillis())
            .apply()
    }

    /** Latest reading, or null if nothing has been measured yet. */
    fun read(context: Context): Reading? {
        val p = prefs(context)
        if (!p.contains(KEY_BPM)) return null
        return Reading(p.getFloat(KEY_BPM, 0f).toDouble(), p.getLong(KEY_UPDATED_AT, 0L))
    }

    data class Reading(val bpm: Double, val updatedAt: Long)
}
