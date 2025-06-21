package com.example.codeforcesreminder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {

    private Switch darkModeSwitch;
    private EditText reminderOffsetEditText;
    private Button saveButton;

    private static final String PREFS_NAME = "CodeforcesPrefs";
    private static final String KEY_REMINDER_OFFSET = "reminderOffset";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        reminderOffsetEditText = findViewById(R.id.reminderOffsetEditText);
        saveButton = findViewById(R.id.saveButton);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load dark mode status
        darkModeSwitch.setChecked(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);

        // Load reminder offset
        int offset = prefs.getInt(KEY_REMINDER_OFFSET, 15);
        reminderOffsetEditText.setText(String.valueOf(offset));

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        saveButton.setOnClickListener(v -> {
            try {
                int newOffset = Integer.parseInt(reminderOffsetEditText.getText().toString());
                if (newOffset <= 0) {
                    Toast.makeText(this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                    return;
                }

                prefs.edit().putInt(KEY_REMINDER_OFFSET, newOffset).apply();

                Toast.makeText(this, "Reminder time saved: " + newOffset + " mins", Toast.LENGTH_SHORT).show();
                finish(); // back to main
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
