package com.example.codeforcesreminder;

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

    private MainActivity mainActivityHelper = new MainActivity(); // for save/load prefs methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        reminderOffsetEditText = findViewById(R.id.reminderOffsetEditText);
        saveButton = findViewById(R.id.saveButton);

        // Load saved settings
        darkModeSwitch.setChecked(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
        reminderOffsetEditText.setText(String.valueOf(mainActivityHelper.getSavedReminderOffset()));

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppCompatDelegate.setDefaultNightMode(isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        saveButton.setOnClickListener(v -> {
            try {
                int offset = Integer.parseInt(reminderOffsetEditText.getText().toString());
                if (offset <= 0) {
                    Toast.makeText(this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                    return;
                }
                mainActivityHelper.saveReminderOffset(offset);
                Toast.makeText(this, "Reminder time saved: " + offset + " mins", Toast.LENGTH_SHORT).show();
                finish();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
