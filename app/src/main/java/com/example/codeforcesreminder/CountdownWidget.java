package com.example.codeforcesreminder;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

public class CountdownWidget extends AppWidgetProvider {

    private static final String PREFS_NAME = "CodeforcesPrefs";
    private static final String KEY_CONTESTS_JSON = "contestsJson";

    private static Handler handler = new Handler(Looper.getMainLooper());
    private static Runnable updateRunnable;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateWidget(context);
    }

    public static void updateWidget(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String contestsJson = prefs.getString(KEY_CONTESTS_JSON, null);
        long now = System.currentTimeMillis();
        String nextContestName = "No upcoming contest";
        long nextContestStart = Long.MAX_VALUE;

        if (contestsJson != null) {
            try {
                JSONArray arr = new JSONArray(contestsJson);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject contest = arr.getJSONObject(i);
                    String phase = contest.getString("phase");
                    if (!"BEFORE".equals(phase)) continue;

                    long startTime = contest.getLong("startTimeSeconds") * 1000;
                    if (startTime > now && startTime < nextContestStart) {
                        nextContestStart = startTime;
                        nextContestName = contest.getString("name");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        long diff = nextContestStart - now;
        String countdownText = "No upcoming contest";
        if (diff > 0 && diff != Long.MAX_VALUE) {
            long seconds = diff / 1000;
            long hrs = seconds / 3600;
            long mins = (seconds % 3600) / 60;
            long secs = seconds % 60;
            countdownText = String.format("Starts in %02d:%02d:%02d", hrs, mins, secs);
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, CountdownWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_countdown);
            views.setTextViewText(R.id.widgetContestName, nextContestName);
            views.setTextViewText(R.id.widgetCountdown, countdownText);

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        // Schedule next update in 1 sec
        if (updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }
        updateRunnable = () -> updateWidget(context);
        handler.postDelayed(updateRunnable, 1000);
    }
}
