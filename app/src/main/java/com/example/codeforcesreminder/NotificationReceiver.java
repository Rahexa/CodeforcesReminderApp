package com.example.codeforcesreminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "codeforces_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {

        String contestName = intent.getStringExtra("contest_name");
        int contestId = intent.getIntExtra("contest_id", 0);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Contest Reminders", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for upcoming Codeforces contests");
            // Unique alarm sound from raw resource
            channel.setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.alarm_sound), audioAttributes);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        Intent contestIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://codeforces.com/contest/" + contestId));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, contestId, contestIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Codeforces Contest Reminder")
                .setContentText(contestName + " is starting soon!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.alarm_sound))
                .setOngoing(true);  // Prevent user dismissal

        notificationManager.notify(contestId, builder.build());
    }
}
