package com.example.zephyr.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.zephyr.R;
import com.example.zephyr.fragments.AccountFragment;
import com.example.zephyr.fragments.FavoritesFragment;
import com.example.zephyr.fragments.HomeFragment;
import com.example.zephyr.fragments.NewsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        Window window = getWindow();

        window.setStatusBarColor(getResources().getColor(R.color.background_dark));

        bottomNavigationView = findViewById(R.id.bottomNavView);
        frameLayout = findViewById(R.id.frameLayout);

        // Set the initial fragment and highlight Home
        loadFragment(new HomeFragment());
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        // Listener for bottom navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    loadFragment(new HomeFragment());
                } else if (itemId == R.id.navigation_favorites) {
                    loadFragment(new FavoritesFragment());
                } else if (itemId == R.id.navigation_news) {
                    loadFragment(new NewsFragment());
                } else if (itemId == R.id.navigation_account) {
                    loadFragment(new AccountFragment());
                }

                return true; // Ensure the selected item is highlighted
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }
}
