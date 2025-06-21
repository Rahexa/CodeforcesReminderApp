package com.example.codeforcesreminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String contestName = intent.getStringExtra("contest_name");
        int contestId = intent.getIntExtra("contest_id", 0);
        long contestStartTime = intent.getLongExtra("contest_start_time", 0);

        Intent serviceIntent = new Intent(context, NotificationService.class);
        serviceIntent.putExtra("contest_name", contestName);
        serviceIntent.putExtra("contest_id", contestId);
        serviceIntent.putExtra("contest_start_time", contestStartTime);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}
