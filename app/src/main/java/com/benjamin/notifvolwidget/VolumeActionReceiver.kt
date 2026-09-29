package com.benjamin.notifvolwidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.widget.Toast

/**
 * Handles the widget's three button taps. It only ever changes the
 * *notification* volume: it never touches the ringer mode, silent/vibrate or
 * Do Not Disturb, so it needs no special permission.
 *
 * Declared android:exported="false" in the manifest: it is reached only
 * through this app's own PendingIntents (built in [VolumeWidgetProvider]), so
 * another app on the device cannot address it directly to change the volume
 * without the user having tapped the widget.
 */
class VolumeActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
        val current = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
        val target = when (action) {
            VolumeWidgetProvider.ACTION_OFF -> 0
            VolumeWidgetProvider.ACTION_VOL_UP -> VolumeLevels.raisedIndex(current, max)
            VolumeWidgetProvider.ACTION_VOL_DOWN -> VolumeLevels.loweredIndex(current, max)
            else -> return
        }

        val applied = try {
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, target, 0)
            audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) == target
        } catch (e: SecurityException) {
            false
        }

        // Android silently ignores volume changes in some states (silent mode,
        // Do Not Disturb). Say so, rather than leaving a button that seems dead.
        if (!applied) {
            Toast.makeText(context, R.string.volume_ignored, Toast.LENGTH_SHORT).show()
        }

        VolumeWidgetProvider.updateAll(context)
    }
}
