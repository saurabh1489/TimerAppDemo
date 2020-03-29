package com.sample.servicedemo.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class TimerBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "TimerBroadcastReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "TimerCancelBroadcastReceiver::onReceive() ${intent?.action}")
        Toast.makeText(
            context,
            "TimerCancelBroadcastReceiver::onReceive() ${intent?.action}",
            Toast.LENGTH_SHORT
        ).show()
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_LOCKED_BOOT_COMPLETED -> {
                Intent(context, TimerService::class.java).apply {
                    action = ACTION_TIMER_RESUME
                    context?.startForegroundService(this)
                }
            }
            ACTION_TIMER_CANCEL -> {
                Intent(context, TimerService::class.java).apply {
                    action = ACTION_TIMER_CANCEL
                    context?.startService(this)
                }
            }
        }

    }
}