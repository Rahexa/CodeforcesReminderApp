package com.example.codeforcesreminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CountdownWidget extends AppWidgetProvider {
    private static final String UPDATE_ACTION = "com.example.codeforcesreminder.UPDATE_WIDGET";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateWidget(context);
        scheduleNextUpdate(context);
    }

    public static void updateWidget(Context context) {
        ContestFetcher.fetchUpcomingContests(context, contests -> {
            if (contests != null && !contests.isEmpty()) {
                Contest nextContest = contests.get(0);
                long timeLeft = nextContest.getStartTime() - System.currentTimeMillis();

                long hours = timeLeft / (1000 * 60 * 60);
                long minutes = (timeLeft / (1000 * 60)) % 60;

                String countdown = String.format(Locale.getDefault(), "Starts in: %02d:%02d", hours, minutes);
                String title = nextContest.getName();

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_countdown);
                views.setTextViewText(R.id.widgetTitle, title);
                views.setTextViewText(R.id.widgetTimeLeft, countdown);

                AppWidgetManager manager = AppWidgetManager.getInstance(context);
                ComponentName widget = new ComponentName(context, CountdownWidget.class);
                manager.updateAppWidget(widget, views);
            }
        });
    }

    private void scheduleNextUpdate(Context context) {
        Intent intent = new Intent(context, CountdownWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long intervalMillis = 60 * 1000; // ১ মিনিট পর পর আপডেট
        long triggerAtMillis = SystemClock.elapsedRealtime() + intervalMillis;
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent);
    }
}
