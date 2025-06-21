package com.example.codeforcesreminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ContestAdapter adapter;
    private List<Contest> contestList;
    private RequestQueue requestQueue;
    private AlarmManager alarmManager;
    private Handler countdownHandler = new Handler();

    private static final String API_URL = "https://codeforces.com/api/contest.list?gym=false";
    private static final String PREFS_NAME = "CodeforcesPrefs";
    private static final String KEY_REMINDER_OFFSET = "reminderOffset";

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
                        JSONArray contestsJson = response.getJSONArray("result");
                        contestList.clear();
                        for (int i = 0; i < contestsJson.length(); i++) {
                            JSONObject contestJson = contestsJson.getJSONObject(i);
                            if ("BEFORE".equals(contestJson.getString("phase"))) {
                                int id = contestJson.getInt("id");
                                String name = contestJson.getString("name");
                                long startTimeMillis = contestJson.getLong("startTimeSeconds") * 1000L;
                                contestList.add(new Contest(id, name, startTimeMillis));
                            }
                        }
                        Collections.sort(contestList, (c1, c2) -> Long.compare(c1.getStartTime(), c2.getStartTime()));

                        adapter.notifyDataSetChanged();

                        // Schedule reminders
                        int reminderOffset = getSavedReminderOffset();
                        scheduleReminders(contestList, reminderOffset);

                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error fetching contests", Toast.LENGTH_SHORT).show());

        requestQueue.add(request);
    }

    private void scheduleReminders(List<Contest> contests, int offsetMinutes) {
        for (Contest contest : contests) {
            long reminderTime = contest.getStartTime() - (offsetMinutes * 60 * 1000);
            if (reminderTime > System.currentTimeMillis()) {
                Intent intent = new Intent(this, NotificationReceiver.class);
                intent.putExtra("contest_name", contest.getName());
                intent.putExtra("contest_id", contest.getId());
                intent.putExtra("contest_start_time", contest.getStartTime());

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this,
                        contest.getId(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
            }
        }
        Toast.makeText(this, "Reminders set for all contests", Toast.LENGTH_SHORT).show();
    }

    private void openContestLink(Contest contest) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://codeforces.com/contest/" + contest.getId()));
        startActivity(browserIntent);
    }

    public int getSavedReminderOffset() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(KEY_REMINDER_OFFSET, 15); // Default 15 minutes
    }

    public void saveReminderOffset(int offset) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putInt(KEY_REMINDER_OFFSET, offset).apply();
    }
}
