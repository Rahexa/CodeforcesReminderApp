package com.example.codeforcesreminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ContestAdapter adapter;
    private List<Contest> contestList;
    private RequestQueue requestQueue;
    private AlarmManager alarmManager;

    private static final String API_URL = "https://codeforces.com/api/contest.list?gym=false";
    private static final String PREFS_NAME = "CodeforcesPrefs";
    private static final String KEY_REMINDER_OFFSET = "reminderOffset";
    private static final String KEY_CONTESTS_JSON = "contestsJson";

    private Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        contestList = new ArrayList<>();
        adapter = new ContestAdapter(contestList, this::openContestLink);
        recyclerView.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(this);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        fetchContests();
    }

    private void fetchContests() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, API_URL, null,
                response -> {
                    try {
                        JSONArray contests = response.getJSONArray("result");
                        contestList.clear();

                        for (int i = 0; i < contests.length(); i++) {
                            JSONObject contest = contests.getJSONObject(i);
                            if ("BEFORE".equals(contest.getString("phase"))) {
                                String name = contest.getString("name");
                                long startTimeMillis = contest.getLong("startTimeSeconds") * 1000;
                                int id = contest.getInt("id");
                                contestList.add(new Contest(id, name, startTimeMillis));
                            }
                        }

                        // Sort by start time
                        Collections.sort(contestList, Comparator.comparingLong(Contest::getStartTime));

                        adapter.notifyDataSetChanged();

                        // Save contests JSON string for widget
                        saveContestsJson(contests.toString());

                        // Schedule reminders
                        int offset = getSavedReminderOffset();
                        scheduleReminders(offset);

                        // Update countdown widget
                        CountdownWidget.updateWidget(this);

                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing contests", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Failed to fetch contests", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(request);
    }

    private void saveContestsJson(String json) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_CONTESTS_JSON, json).apply();
    }

    public int getSavedReminderOffset() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(KEY_REMINDER_OFFSET, 15);
    }

    public void saveReminderOffset(int offset) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putInt(KEY_REMINDER_OFFSET, offset).apply();
    }

    private void scheduleReminders(int offsetMinutes) {
        for (Contest contest : contestList) {
            long reminderTime = contest.getStartTime() - (offsetMinutes * 60 * 1000);
            if (reminderTime > System.currentTimeMillis()) {
                Intent intent = new Intent(this, NotificationReceiver.class);
                intent.putExtra("contest_name", contest.getName());
                intent.putExtra("contest_id", contest.getId());

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this,
                        contest.getId(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
            }
        }
        Toast.makeText(this, "Reminders scheduled", Toast.LENGTH_SHORT).show();
    }

    private void openContestLink(Contest contest) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://codeforces.com/contest/" + contest.getId()));
        startActivity(browserIntent);
    }

    // Refresh countdown timers every second in UI
    @Override
    protected void onResume() {
        super.onResume();
        uiHandler.postDelayed(updateRunnable, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHandler.removeCallbacks(updateRunnable);
    }

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            adapter.notifyDataSetChanged();
            uiHandler.postDelayed(this, 1000);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
