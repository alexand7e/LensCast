package com.opencode.multilensipcam

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StreamControlReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_START_STREAM,
            ACTION_STOP_STREAM,
            ACTION_TOGGLE_STREAM -> {
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    action = intent.action
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra(EXTRA_FROM_BROADCAST, true)
                }
                context.startActivity(launchIntent)
            }
        }
    }

    companion object {
        const val ACTION_START_STREAM = "com.opencode.multilensipcam.START"
        const val ACTION_STOP_STREAM = "com.opencode.multilensipcam.STOP"
        const val ACTION_TOGGLE_STREAM = "com.opencode.multilensipcam.TOGGLE"
        const val EXTRA_FROM_BROADCAST = "from_broadcast"
    }
}