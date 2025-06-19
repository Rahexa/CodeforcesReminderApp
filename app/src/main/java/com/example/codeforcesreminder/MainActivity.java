package com.example.codeforcesreminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
    private static final String KEY_DARK_MODE = "darkMode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean darkModeEnabled = prefs.getBoolean(KEY_DARK_MODE, false);
        AppCompatDelegate.setDefaultNightMode(darkModeEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

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
                                long startTimeSeconds = contest.getLong("startTimeSeconds") * 1000;
                                int id = contest.getInt("id");
                                contestList.add(new Contest(id, name, startTimeSeconds));
                            }
                        }
                        Collections.sort(contestList, Comparator.comparingLong(Contest::getStartTime));
                        adapter.notifyDataSetChanged();

                        scheduleReminders(contestList);
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing contest data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error fetching contests", Toast.LENGTH_SHORT).show()
        );
        requestQueue.add(request);
    }

    private int[] getSavedReminderOffsets() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String offsetsString = prefs.getString(KEY_REMINDER_OFFSET, "60,15,5");
        try {
            String[] parts = offsetsString.split(",");
            int[] offsets = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                offsets[i] = Integer.parseInt(parts[i].trim());
            }
            return offsets;
        } catch (Exception e) {
            return new int[]{60, 15, 5};
        }
    }

    private void scheduleReminders(List<Contest> contests) {
        int[] offsets = getSavedReminderOffsets();

        for (Contest contest : contests) {
            for (int offsetMinutes : offsets) {
                long reminderTime = contest.getStartTime() - offsetMinutes * 60 * 1000L;
                if (reminderTime > System.currentTimeMillis()) {
                    Intent intent = new Intent(this, NotificationReceiver.class);
                    intent.putExtra("contest_name", contest.getName());
                    intent.putExtra("contest_id", contest.getId());
                    intent.putExtra("offset", offsetMinutes);

                    int notificationId = contest.getId() * 1000 + offsetMinutes;
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            this, notificationId, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
                }
            }
        }
        Toast.makeText(this, "Reminders set!", Toast.LENGTH_SHORT).show();
    }

    private void openContestLink(Contest contest) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://codeforces.com/contest/" + contest.getId()));
        startActivity(browserIntent);
    }

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
