package com.nusv.lite.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.nusv.lite.MainActivity
import com.nusv.lite.R
import com.nusv.lite.util.PointsManager

class NusvWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_CHECK_IN -> {
                PointsManager.checkIn(context)
                refresh(context)
            }
            // The check-in button resets at midnight, so refresh when the day
            // (or the time zone defining it) changes without opening the app.
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> refresh(context)
        }
    }

    companion object {
        const val ACTION_CHECK_IN = "com.nusv.lite.action.CHECK_IN"

        /**
         * Pushes the latest points / streak / check-in state to every placed
         * widget. PointsManager calls this after any balance change, because
         * the widget has updatePeriodMillis=0 and would otherwise show stale
         * data until it is re-added or tapped.
         */
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, NusvWidgetProvider::class.java))
            ids.forEach { updateWidget(context, manager, it) }
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_nusv)
            val balance = PointsManager.getBalance(context)
            val streak = PointsManager.getStreak(context)
            val canCheckIn = PointsManager.canCheckIn(context)

            views.setTextViewText(
                R.id.widget_points,
                context.getString(R.string.widget_points_value, balance)
            )
            views.setTextViewText(
                R.id.widget_streak,
                context.getString(R.string.widget_streak_value, streak)
            )
            views.setTextViewText(
                R.id.widget_checkin_btn,
                context.getString(if (canCheckIn) R.string.widget_checkin else R.string.widget_checkin_done)
            )

            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openPi = PendingIntent.getActivity(
                context, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openPi)

            val checkinIntent = Intent(context, NusvWidgetProvider::class.java).apply {
                action = ACTION_CHECK_IN
            }
            val checkinPi = PendingIntent.getBroadcast(
                context, 1, checkinIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_checkin_btn, checkinPi)

            manager.updateAppWidget(widgetId, views)
        }
    }
}
