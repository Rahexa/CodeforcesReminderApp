package com.example.codeforcesreminder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "CodeforcesPrefs";
    private static final String KEY_REMINDER_OFFSET = "reminderOffset";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Switch darkModeSwitch = findViewById(R.id.darkModeSwitch);
        EditText reminderOffsetEditText = findViewById(R.id.reminderOffsetEditText);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize UI elements
        darkModeSwitch.setChecked(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
        reminderOffsetEditText.setText(String.valueOf(prefs.getInt(KEY_REMINDER_OFFSET, 15)));

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        reminderOffsetEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                try {
                    int offset = Integer.parseInt(reminderOffsetEditText.getText().toString());
                    prefs.edit().putInt(KEY_REMINDER_OFFSET, offset).apply();
                } catch (NumberFormatException e) {
                    reminderOffsetEditText.setError("Enter a valid number");
                }
            }
        });
    }
}
