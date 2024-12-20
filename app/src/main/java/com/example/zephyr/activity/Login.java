package com.example.zephyr.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zephyr.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.TimeUnit;

public class Login extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private MaterialButton loginButton;
    private ProgressBar progressBar;
    private CheckBox autoLogInCheckbox;
    private TextView signUpText;

    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    private static final long ONE_WEEK_MILLIS = TimeUnit.DAYS.toMillis(7);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI elements
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        autoLogInCheckbox = findViewById(R.id.autoLogInCheckbox);
        signUpText = findViewById(R.id.signUpText);

        // Button to move to Registration
        signUpText.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, Registration.class));
            finish();
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("AutoLoginPrefs", MODE_PRIVATE);

        // Check inactivity period and auto-logout if needed
        checkInactivityAndLogout();

        // Check if Auto Log In is enabled
        if (sharedPreferences.getBoolean("autoLogIn", false)) {
            autoLogin();
        }

        loginButton.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);

            if (task.isSuccessful()) {
                if (autoLogInCheckbox.isChecked()) {
                    sharedPreferences.edit()
                            .putBoolean("autoLogIn", true)
                            .putLong("lastActiveTime", System.currentTimeMillis()) // Save current time
                            .apply();
                }
                navigateToMain();
            } else {
                Toast.makeText(Login.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void autoLogin() {
        if (mAuth.getCurrentUser() != null) {
            navigateToMain();
        }
    }

    private void checkInactivityAndLogout() {
        long lastActiveTime = sharedPreferences.getLong("lastActiveTime", 0);
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastActiveTime > ONE_WEEK_MILLIS) {
            // Inactive for more than a week, log out the user
            sharedPreferences.edit().clear().apply(); // Clear preferences
            mAuth.signOut();
        }
    }

    private void navigateToMain() {
        sharedPreferences.edit().putLong("lastActiveTime", System.currentTimeMillis()).apply(); // Update last active time
        Intent intent = new Intent(Login.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
