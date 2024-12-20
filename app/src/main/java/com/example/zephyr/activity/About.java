package com.example.zephyr.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zephyr.R;

public class About extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the about_section.xml as the content view
        setContentView(R.layout.activity_about);
    }
}
