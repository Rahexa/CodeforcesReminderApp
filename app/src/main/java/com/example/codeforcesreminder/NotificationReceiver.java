package com.example.codeforcesreminder;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "codeforces_reminder";
    private static final String ACTION_SNOOZE = "com.example.codeforcesreminder.ACTION_SNOOZE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_SNOOZE.equals(intent.getAction())) {
            handleSnooze(context, intent);
            return;
        }

        String contestName = intent.getStringExtra("contest_name");
        int contestId = intent.getIntExtra("contest_id", 0);
        int offset = intent.getIntExtra("offset", 15);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Contest Reminders", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for Codeforces contests");
            Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.custom_sound);
            channel.setSound(soundUri, null);
            notificationManager.createNotificationChannel(channel);
        }

        Intent contestIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://codeforces.com/contest/" + contestId));
        PendingIntent contestPendingIntent = PendingIntent.getActivity(context, contestId, contestIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent snoozeIntent = new Intent(context, NotificationReceiver.class);
        snoozeIntent.setAction(ACTION_SNOOZE);
        snoozeIntent.putExtra("contest_name", contestName);
        snoozeIntent.putExtra("contest_id", contestId);
        snoozeIntent.putExtra("offset", offset);

        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context, contestId + 9999, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Contest Reminder: " + contestName)
                .setContentText("Starting in " + offset + " minutes!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contestPendingIntent)
                .addAction(android.R.drawable.ic_media_pause, "Snooze 5 min", snoozePendingIntent)
                .setAutoCancel(true)
                .setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.custom_sound))
                .setOnlyAlertOnce(true);

        notificationManager.notify(contestId * 1000 + offset, builder.build());
    }

    private void handleSnooze(Context context, Intent intent) {
        int contestId = intent.getIntExtra("contest_id", 0);
        int offset = intent.getIntExtra("offset", 15);
        String contestName = intent.getStringExtra("contest_name");

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent newIntent = new Intent(context, NotificationReceiver.class);
        newIntent.putExtra("contest_name", contestName);
        newIntent.putExtra("contest_id", contestId);
        newIntent.putExtra("offset", offset);

        int notificationId = contestId * 1000 + offset;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, notificationId, newIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long snoozeTime = System.currentTimeMillis() + 5 * 60 * 1000L; // 5 minutes snooze
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent);
    }
}
