package com.example.codeforcesreminder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "CodeforcesPrefs";
    private static final String KEY_REMINDER_OFFSET = "reminderOffset";

    private EditText reminderOffsetEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        reminderOffsetEditText = findViewById(R.id.reminderOffsetEditText);
        saveButton = findViewById(R.id.saveButton);

        // SharedPreferences থেকে আগে সেট করা মান নিয়ে আসা
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedOffset = prefs.getInt(KEY_REMINDER_OFFSET, 15);  // default 15 mins
        reminderOffsetEditText.setText(String.valueOf(savedOffset));

        // Save বাটনে ক্লিক করলে মান সংরক্ষণ
        saveButton.setOnClickListener(v -> {
            String input = reminderOffsetEditText.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(SettingsActivity.this, "Please enter a reminder offset", Toast.LENGTH_SHORT).show();
                return;
            }

            int offset;
            try {
                offset = Integer.parseInt(input);
                if (offset <= 0) {
                    Toast.makeText(SettingsActivity.this, "Enter a positive number", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(SettingsActivity.this, "Invalid number format", Toast.LENGTH_SHORT).show();
                return;
            }

            // SharedPreferences-এ নতুন মান সংরক্ষণ করা হচ্ছে
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_REMINDER_OFFSET, offset);
            editor.apply();

            Toast.makeText(SettingsActivity.this, "Reminder offset saved: " + offset + " minutes", Toast.LENGTH_SHORT).show();
        });
    }
}
