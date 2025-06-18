package com.example.codeforcesreminder;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {
    private MainActivity mainActivityInstance; // Field to hold the instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Switch darkModeSwitch = findViewById(R.id.darkModeSwitch);
        EditText reminderOffsetEditText = findViewById(R.id.reminderOffsetEditText);

        // Initialize mainActivityInstance (workaround, improve later if possible)
        mainActivityInstance = new MainActivity(); // Temporary instance

        // Load saved values
        darkModeSwitch.setChecked(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
        reminderOffsetEditText.setText(String.valueOf(mainActivityInstance.getSavedReminderOffset()));

        // Set listeners
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        reminderOffsetEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                try {
                    String offsetText = reminderOffsetEditText.getText().toString();
                    int offset = Integer.parseInt(offsetText.isEmpty() ? "15" : offsetText); // Default to 15 if empty
                    mainActivityInstance.saveReminderOffset(offset);
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
