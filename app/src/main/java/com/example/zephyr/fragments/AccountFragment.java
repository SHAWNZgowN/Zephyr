package com.example.zephyr.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.zephyr.R;
import com.example.zephyr.activity.About;
import com.example.zephyr.activity.Login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountFragment extends Fragment {

    private TextView usernameTextView;
    private TextView emailTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_account, container, false);

        // Initialize views
        usernameTextView = rootView.findViewById(R.id.usernameTextView);
        emailTextView = rootView.findViewById(R.id.emailTextView);

        // Fetch user details from Firebase
        fetchUserDetails();

        // Set up the click listener for the log-out text view
        rootView.findViewById(R.id.logOut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogOutClicked();
            }
        });

        // Set up the click listener for the About button
        rootView.findViewById(R.id.aboutBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAboutActivity();
            }
        });

        return rootView;
    }

    private void openAboutActivity() {
        Intent intent = new Intent(getContext(), About.class);
        startActivity(intent);
    }

    private void fetchUserDetails() {
        // Get the current user from FirebaseAuth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Set the email directly from FirebaseAuth
            emailTextView.setText(currentUser.getEmail());

            // Fetch additional details from Firestore (if needed)
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Set the username from Firestore
                                String username = document.getString("username");
                                usernameTextView.setText(username);
                            } else {
                                Toast.makeText(getContext(), "User details not found in database.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Error fetching user details.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // If no user is logged in, redirect to the login screen
            logOut();
        }
    }

    // Method that handles the log-out button click
    public void onLogOutClicked() {
        // Create an AlertDialog for confirmation
        new AlertDialog.Builder(getContext())
                .setMessage("Are you sure you want to log out?")
                .setCancelable(false) // Prevents closing the dialog when clicked outside
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If the user confirms, log out
                        logOut();
                    }
                })
                .setNegativeButton("No", null)  // Close the dialog if the user clicks "No"
                .show();
    }

    // Handle the log-out action
    private void logOut() {
        try {
            // Sign out the user from FirebaseAuth
            FirebaseAuth.getInstance().signOut();

            // Clear user session for security
            clearUserSession();

            // Navigate back to LoginActivity
            Intent intent = new Intent(getContext(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Prevents going back to MainActivity
            startActivity(intent);
            requireActivity().finish(); // Close current fragment/activity

        } catch (Exception e) {
            // Error handling: Show a message to the user if something goes wrong
            Toast.makeText(getContext(), "Error during logout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Clear user session and auto-login preference
    private void clearUserSession() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AutoLoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Clear all saved preferences
        editor.apply();

        Toast.makeText(getContext(), "Session cleared. Logged out successfully.", Toast.LENGTH_SHORT).show();
    }
}
