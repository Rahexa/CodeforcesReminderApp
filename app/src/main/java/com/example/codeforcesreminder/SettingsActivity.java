package com.example.codeforcesreminder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private EditText reminderOffsetsEditText;
    private Switch darkModeSwitch;
    private Button saveButton;

    private static final String PREFS_NAME = "CodeforcesPrefs";
    private static final String KEY_REMINDER_OFFSET = "reminderOffset";
    private static final String KEY_DARK_MODE = "darkMode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        reminderOffsetsEditText = findViewById(R.id.reminderOffsetsEditText);
        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        saveButton = findViewById(R.id.saveButton);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String offsets = prefs.getString(KEY_REMINDER_OFFSET, "60,15,5");
        boolean darkModeEnabled = prefs.getBoolean(KEY_DARK_MODE, false);

        reminderOffsetsEditText.setText(offsets);
        darkModeSwitch.setChecked(darkModeEnabled);

        saveButton.setOnClickListener(v -> {
            String input = reminderOffsetsEditText.getText().toString();
            if (validateOffsets(input)) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(KEY_REMINDER_OFFSET, input);
                editor.putBoolean(KEY_DARK_MODE, darkModeSwitch.isChecked());
                editor.apply();

                Toast.makeText(this, "Settings saved. Restart app for changes.", Toast.LENGTH_SHORT).show();
            } else {
                reminderOffsetsEditText.setError("Invalid format! Use comma-separated positive numbers.");
            }
        });
    }

    private boolean validateOffsets(String input) {
        String[] parts = input.split(",");
        for (String p : parts) {
            try {
                int val = Integer.parseInt(p.trim());
                if (val <= 0) return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }
}
