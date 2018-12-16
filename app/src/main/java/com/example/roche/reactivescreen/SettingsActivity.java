package com.example.roche.reactivescreen;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final EditText mEdit = findViewById(R.id.goal);

        SharedPreferences settings = getSharedPreferences("settings", 0);
        int goal = settings.getInt("steps", 2000);

        mEdit.setText(String.valueOf(goal));

        final Button button = findViewById(R.id.submit);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int val = Integer.parseInt(mEdit.getText().toString());
                SharedPreferences settings = getSharedPreferences("settings", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("steps", val);
                editor.apply();
            }
        });
    }
}
