package com.example.codeforcesreminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {
    public static final String CHANNEL_ID = "contest_notification_channel";
    private static final int NOTIFICATION_ID = 1001;

    private String contestName;
    private int contestId;
    private long contestStartTime;

    private Timer timer;
    private Handler handler = new Handler();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        contestName = intent.getStringExtra("contest_name");
        contestId = intent.getIntExtra("contest_id", 0);
        contestStartTime = intent.getLongExtra("contest_start_time", 0);

        createNotificationChannel();

        // Initial notification
        startForeground(NOTIFICATION_ID, buildNotification("Loading countdown..."));

        // Start updating countdown
        startCountdown();

        return START_NOT_STICKY;
    }

    private void startCountdown() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> {
                    long now = System.currentTimeMillis();
                    long diff = contestStartTime - now;

                    if (diff <= 0) {
                        stopSelf(); // Contest started, stop notification
                        return;
                    }

                    long seconds = (diff / 1000) % 60;
                    long minutes = (diff / (1000 * 60)) % 60;
                    long hours = diff / (1000 * 60 * 60);

                    String countdown = String.format("Starts in: %02d hr %02d min %02d sec", hours, minutes, seconds);

                    Notification notification = buildNotification(countdown);
                    NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    manager.notify(NOTIFICATION_ID, notification);
                });
            }
        }, 0, 1000); // Update every 1 second
    }

    private Notification buildNotification(String countdownText) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://codeforces.com/contest/" + contestId));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Contest: " + contestName)
                .setContentText(countdownText)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setOngoing(true) // Prevent swipe dismiss
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.unique_alarm);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Contest Countdown",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setSound(soundUri, audioAttributes);
            channel.setDescription("Shows countdown to upcoming contest");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
