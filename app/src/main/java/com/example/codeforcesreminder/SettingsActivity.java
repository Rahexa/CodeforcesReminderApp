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

    private static final String PREFS_NAME = "CodeforcesPrefs";
    private static final String KEY_REMINDER_OFFSET = "reminderOffset";

    private EditText reminderOffsetEditText;
    private Button saveButton;
    private Switch darkModeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        reminderOffsetEditText = findViewById(R.id.reminderOffsetEditText);
        saveButton = findViewById(R.id.saveButton);
        darkModeSwitch = findViewById(R.id.darkModeSwitch);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedOffset = prefs.getInt(KEY_REMINDER_OFFSET, 15);
        reminderOffsetEditText.setText(String.valueOf(savedOffset));

        // Load dark mode preference
        boolean isDark = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        darkModeSwitch.setChecked(isDark);

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        saveButton.setOnClickListener(v -> {
            String input = reminderOffsetEditText.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(this, "Please enter a value", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int offset = Integer.parseInt(input);
                if (offset <= 0) {
                    Toast.makeText(this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                    return;
                }

                prefs.edit().putInt(KEY_REMINDER_OFFSET, offset).apply();
                Toast.makeText(this, "Saved: " + offset + " mins", Toast.LENGTH_SHORT).show();
                finish();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
