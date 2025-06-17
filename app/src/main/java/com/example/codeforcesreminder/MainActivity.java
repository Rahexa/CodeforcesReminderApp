package com.example.codeforcesreminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ContestAdapter adapter;
    private List<Contest> contestList;
    private RequestQueue requestQueue;
    private static final String API_URL = "https://codeforces.com/api/contest.list?gym=false";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        contestList = new ArrayList<>();
        adapter = new ContestAdapter(contestList, contest -> {
            // Open contest link
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://codeforces.com/contest/" + contest.getId()));
            startActivity(browserIntent);
        }, this::scheduleNotification);
        recyclerView.setAdapter(adapter);
        requestQueue = Volley.newRequestQueue(this);
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
                            if (contest.getString("phase").equals("BEFORE")) {
                                String name = contest.getString("name");
                                long startTimeSeconds = contest.getLong("startTimeSeconds") * 1000;
                                int id = contest.getInt("id");
                                contestList.add(new Contest(id, name, startTimeSeconds));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error fetching contests", Toast.LENGTH_SHORT).show());
        requestQueue.add(request);
    }

    private void scheduleNotification(Contest contest, long reminderTime) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("contest_name", contest.getName());
        intent.putExtra("contest_id", contest.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, contest.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        Toast.makeText(this, "Reminder set for " + contest.getName(), Toast.LENGTH_SHORT).show();
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
