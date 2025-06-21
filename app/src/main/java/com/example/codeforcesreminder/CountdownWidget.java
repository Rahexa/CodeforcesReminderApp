package com.example.codeforcesreminder;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

public class CountdownWidget extends AppWidgetProvider {

    private static final String PREFS_NAME = "CodeforcesPrefs";
    private static final String KEY_CONTESTS_JSON = "contestsJson";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId);
        }
    }

    public static void updateWidget(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new android.content.ComponentName(context, CountdownWidget.class));
        for (int widgetId : ids) {
            updateWidget(context, manager, widgetId);
        }
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String contestsJson = prefs.getString(KEY_CONTESTS_JSON, "");

        String contestName = "No upcoming contests";
        String countdown = "";

        try {
            JSONArray contests = new JSONArray(contestsJson);
            long now = System.currentTimeMillis();

            long closestStartTime = Long.MAX_VALUE;
            String nextContest = "";

            for (int i = 0; i < contests.length(); i++) {
                JSONObject contest = contests.getJSONObject(i);
                String phase = contest.optString("phase", "");
                long startTime = contest.optLong("startTimeSeconds", 0) * 1000;

                if ("BEFORE".equals(phase) && startTime > now && startTime < closestStartTime) {
                    closestStartTime = startTime;
                    nextContest = contest.optString("name", "");
                }
            }

            if (!nextContest.isEmpty()) {
                contestName = nextContest;
                long diff = closestStartTime - now;
                countdown = formatMillis(diff);
            }
        } catch (Exception ignored) {}

        views.setTextViewText(R.id.widget_contest_name, contestName);
        views.setTextViewText(R.id.widget_countdown, countdown.isEmpty() ? "" : "Starts in: " + countdown);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);

        manager.updateAppWidget(widgetId, views);
    }

    private static String formatMillis(long millis) {
        long seconds = millis / 1000;
        long hrs = seconds / 3600;
        long mins = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hrs, mins, secs);
    }
}
