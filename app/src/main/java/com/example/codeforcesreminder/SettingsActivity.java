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

        // Load saved values using the application context
        MainActivity mainActivity = (MainActivity) getIntent().getParcelableExtra("mainActivity");
        if (mainActivity == null) {
            // Fallback to creating a new instance (not ideal, but works for now)
            mainActivity = new MainActivity();
        }

        darkModeSwitch.setChecked(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
        reminderOffsetEditText.setText(String.valueOf(mainActivity.getSavedReminderOffset()));

        // Set listeners
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        reminderOffsetEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                try {
                    int offset = Integer.parseInt(reminderOffsetEditText.getText().toString());
                    mainActivity.saveReminderOffset(offset);
                    // Restart activity to apply changes
                    finish();
                    startActivity(getIntent());
                } catch (NumberFormatException e) {
                    reminderOffsetEditText.setError("Please enter a valid number");
                }
            }
        });
    }
}
