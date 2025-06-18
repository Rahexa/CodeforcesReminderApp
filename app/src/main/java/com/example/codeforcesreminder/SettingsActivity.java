package com.example.codeforcesreminder;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Switch darkModeSwitch = findViewById(R.id.darkModeSwitch);
        EditText reminderOffsetEditText = findViewById(R.id.reminderOffsetEditText);
        MainActivity mainActivity = new MainActivity(); // Temporary instance to access method

        // Load saved values
        darkModeSwitch.setChecked(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
        reminderOffsetEditText.setText(String.valueOf(mainActivity.getSavedReminderOffset()));

        // Set listeners
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        reminderOffsetEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                int offset = Integer.parseInt(reminderOffsetEditText.getText().toString());
                mainActivity.saveReminderOffset(offset); // Save new offset
                // Restart activity to apply changes (simplified approach)
                finish();
                startActivity(getIntent());
            }
        });
    }
}
