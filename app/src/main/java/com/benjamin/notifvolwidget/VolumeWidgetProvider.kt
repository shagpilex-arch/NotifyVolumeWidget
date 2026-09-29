package com.benjamin.notifvolwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.widget.RemoteViews

/**
 * The widget itself: a "Sound off" button, a - / + stepper and a bar showing
 * the current notification volume in round 10% levels. This class only draws
 * the widget and handles the lifecycle broadcasts the system sends
 * (APPWIDGET_UPDATE and friends), which is why it must stay exported. The code
 * that actually changes the volume is [VolumeActionReceiver], which does not
 * need to be exported and isn't.
 */
class VolumeWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_OFF = "com.benjamin.notifvolwidget.ACTION_OFF"
        const val ACTION_VOL_UP = "com.benjamin.notifvolwidget.ACTION_VOL_UP"
        const val ACTION_VOL_DOWN = "com.benjamin.notifvolwidget.ACTION_VOL_DOWN"

        /** Rebuilds and pushes fresh RemoteViews to every placed instance of this widget. */
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, VolumeWidgetProvider::class.java)
            for (id in manager.getAppWidgetIds(thisWidget)) {
                manager.updateAppWidget(id, buildRemoteViews(context))
            }
        }

        private fun buildRemoteViews(context: Context): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            // The bar and its label always show a round 10% level (or "Off"),
            // however the phone's own volume steps happen to divide up.
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
            val index = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
            val level = VolumeLevels.levelOf(index, max)
            val label = if (level == 0) context.getString(R.string.off) else "$level%"

            views.setProgressBar(R.id.progress_volume, 100, level, false)
            views.setTextViewText(R.id.txt_volume, label)
            views.setContentDescription(
                R.id.progress_volume,
                context.getString(R.string.volume_description, label)
            )

            views.setOnClickPendingIntent(R.id.btn_off, actionPendingIntent(context, ACTION_OFF))
            views.setOnClickPendingIntent(R.id.btn_vol_up, actionPendingIntent(context, ACTION_VOL_UP))
            views.setOnClickPendingIntent(R.id.btn_vol_down, actionPendingIntent(context, ACTION_VOL_DOWN))

            return views
        }

        /** Routes a button tap to [VolumeActionReceiver], which is not exported -
         *  only this app's own PendingIntents can reach it, other apps can't. */
        private fun actionPendingIntent(context: Context, action: String): PendingIntent {
            val intent = Intent(context, VolumeActionReceiver::class.java).apply {
                this.action = action
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            // Unique request code per action so the PendingIntents don't collide.
            return PendingIntent.getBroadcast(context, action.hashCode(), intent, flags)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            appWidgetManager.updateAppWidget(id, buildRemoteViews(context))
        }
    }
}
