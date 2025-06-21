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

    private static final String CF_API_URL = "https://codeforces.com/api/contest.list?gym=false";
    private static final String CC_API_URL = "https://www.codechef.com/api/list/contests/all";

    private static final String PREFS_NAME = "CodeforcesPrefs";
    private static final String KEY_REMINDER_OFFSETS = "reminderOffsets"; // store 3 reminder times comma separated
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

        fetchAllContests();
    }

    private void fetchAllContests() {
        fetchContestsFromCodeforces(() -> fetchContestsFromCodeChef(() -> {
            // After fetching both, update UI and schedule reminders
            Collections.sort(contestList, Comparator.comparingLong(Contest::getStartTime));
            adapter.notifyDataSetChanged();

            saveContestsJson();

            scheduleAllReminders();

            CountdownWidget.updateWidget(this);
        }));
    }

    private void fetchContestsFromCodeforces(Runnable callback) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, CF_API_URL, null,
                response -> {
                    try {
                        JSONArray contests = response.getJSONArray("result");
                        for (int i = 0; i < contests.length(); i++) {
                            JSONObject contest = contests.getJSONObject(i);
                            if ("BEFORE".equals(contest.getString("phase"))) {
                                String name = contest.getString("name");
                                long startTimeMillis = contest.getLong("startTimeSeconds") * 1000;
                                int id = contest.getInt("id");
                                contestList.add(new Contest(id, name, startTimeMillis, "codeforces"));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    callback.run();
                },
                error -> {
                    Toast.makeText(this, "Failed to fetch Codeforces contests", Toast.LENGTH_SHORT).show();
                    callback.run();
                });
        requestQueue.add(request);
    }

    private void fetchContestsFromCodeChef(Runnable callback) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, CC_API_URL, null,
                response -> {
                    try {
                        JSONArray contests = response.getJSONArray("data");
                        for (int i = 0; i < contests.length(); i++) {
                            JSONObject contest = contests.getJSONObject(i);
                            String status = contest.optString("status");
                            if ("UPCOMING".equals(status)) {
                                String name = contest.optString("name");
                                long startTimeMillis = contest.optLong("start_date_unix", 0) * 1000;
                                String code = contest.optString("code");
                                // Use code.hashCode as ID (unique)
                                int id = code.hashCode();
                                contestList.add(new Contest(id, name, startTimeMillis, "codechef"));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    callback.run();
                },
                error -> {
                    Toast.makeText(this, "Failed to fetch CodeChef contests", Toast.LENGTH_SHORT).show();
                    callback.run();
                });
        requestQueue.add(request);
    }

    private void saveContestsJson() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Contest c : contestList) {
                JSONObject obj = new JSONObject();
                obj.put("id", c.getId());
                obj.put("name", c.getName());
                obj.put("startTimeSeconds", c.getStartTime() / 1000);
                obj.put("phase", "BEFORE");
                obj.put("platform", c.getPlatform());
                jsonArray.put(obj);
            }
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putString(KEY_CONTESTS_JSON, jsonArray.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Integer> getSavedReminderOffsets() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String offsetsString = prefs.getString(KEY_REMINDER_OFFSETS, "60,30,10");
        String[] parts = offsetsString.split(",");
        List<Integer> offsets = new ArrayList<>();
        try {
            for (String part : parts) {
                offsets.add(Integer.parseInt(part.trim()));
            }
        } catch (Exception e) {
            offsets.clear();
            offsets.add(60);
            offsets.add(30);
            offsets.add(10);
        }
        return offsets;
    }

    private void scheduleAllReminders() {
        List<Integer> offsets = getSavedReminderOffsets();
        alarmManager.cancelAllAlarms(this);

        for (Contest contest : contestList) {
            long startTime = contest.getStartTime();
            for (int i = 0; i < offsets.size(); i++) {
                int offset = offsets.get(i);
                long reminderTime = startTime - offset * 60 * 1000L;
                if (reminderTime > System.currentTimeMillis()) {
                    Intent intent = new Intent(this, NotificationReceiver.class);
                    intent.putExtra("contest_name", contest.getName());
                    intent.putExtra("contest_id", contest.getId());
                    intent.putExtra("notification_id", contest.getId() * 10 + i); // unique notification id

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                            contest.getId() * 10 + i, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
                }
            }
        }
        Toast.makeText(this, "Reminders scheduled for all contests", Toast.LENGTH_SHORT).show();
    }

    private void openContestLink(Contest contest) {
        String url = "";
        if ("codeforces".equals(contest.getPlatform())) {
            url = "https://codeforces.com/contest/" + contest.getId();
        } else if ("codechef".equals(contest.getPlatform())) {
            url = "https://www.codechef.com/contests/" + contest.getId();
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
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
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
