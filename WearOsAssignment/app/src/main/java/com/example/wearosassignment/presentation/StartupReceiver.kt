package com.example.wearosassignment.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * A passive listener registration does not survive a reboot, so the tile and the complication
 * would stop receiving heart rate until the user next opened the app. Re-registering on boot keeps
 * both surfaces live on their own.
 */
class StartupReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        Log.d("StartupReceiver", "Boot completed; re-registering heart rate monitoring")
        HeartRateMonitor.start(context)
    }
}
