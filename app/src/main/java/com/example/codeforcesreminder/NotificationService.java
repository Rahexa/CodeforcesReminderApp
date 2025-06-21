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

        startForeground(NOTIFICATION_ID, buildNotification("Loading countdown..."));

        startCountdown();

        return START_NOT_STICKY;
    }

    private void startCountdown() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post
