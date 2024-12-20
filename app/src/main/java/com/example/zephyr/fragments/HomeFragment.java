package com.example.zephyr.fragments;

import static com.example.zephyr.utils.WeatherUtils.getWeatherIcon;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.inputmethod.EditorInfo;

import com.example.zephyr.R;
import com.example.zephyr.activity.Forecast5Days;
import com.example.zephyr.adapter.HourlyForecastAdapter;
import com.example.zephyr.api.RetrofitClient;
import com.example.zephyr.api.WeatherAPI;
import com.example.zephyr.api.model.WeatherResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private TextView tvLocation, tvTemperature, tvWeatherDescription, tvHumidity, tvWindSpeed, tvDate,
            tvRainPercentage, weatherIndicator, next5Days, tvFeelsLike;
    ImageButton btnFavorites;
    private ImageView ivWeatherIcon;
    private EditText etSearch;
    private WeatherAPI weatherAPI;
    private HourlyForecastAdapter adapter;
    private RecyclerView rvHourlyForecast;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean isUsingSearchLocation = false;
    private String selectedLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for the fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize UI components
        tvLocation = view.findViewById(R.id.tvLocation);
        tvTemperature = view.findViewById(R.id.tvTemperature);
        tvWeatherDescription = view.findViewById(R.id.tvWeatherDescription);
        tvHumidity = view.findViewById(R.id.tvHumidity);
        tvWindSpeed = view.findViewById(R.id.tvWindSpeed);
        tvDate = view.findViewById(R.id.tvDate);
        tvRainPercentage = view.findViewById(R.id.tvRainPercentage);
        weatherIndicator = view.findViewById(R.id.weatherIndicator);
        next5Days = view.findViewById(R.id.next_5days);
        ivWeatherIcon = view.findViewById(R.id.ivWeatherIcon);
        etSearch = view.findViewById(R.id.etSearch);
        tvFeelsLike = view.findViewById(R.id.tvFeelsLike);
        btnFavorites = view.findViewById(R.id.btnFavorites);
        
        // Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();


        btnFavorites.setOnClickListener(v -> {
            String location = tvLocation.getText().toString(); // Get location text
            String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : ""; // Get the current user's UID

            // Ensure user is authenticated
            if (userId.isEmpty()) {
                Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
                return;
            }

            // Reference to the user's favorites collection
            db.collection("users").document(userId).collection("favorites")
                    .document(location) // Use the location as the document ID
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // If the location already exists in favorites, show a toast
                            Toast.makeText(getContext(), "This location is already in your favorites!", Toast.LENGTH_SHORT).show();
                            btnFavorites.setImageResource(R.drawable.ic_added_favorites); // Set to added state
                        } else {
                            // Add the location to favorites
                            db.collection("users").document(userId)
                                    .collection("favorites")
                                    .document(location)
                                    .set(new HashMap<String, Object>() {{
                                        put("location", location);
                                        put("timestamp", System.currentTimeMillis()); // Add timestamp
                                    }})
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Location added to favorites!", Toast.LENGTH_SHORT).show();
                                        btnFavorites.setImageResource(R.drawable.ic_added_favorites); // Change button state
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Failed to add location to favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error checking favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });


        next5Days.setOnClickListener(v -> {
            // Navigate to Forecast5Days activity
            Intent intent = new Intent(getActivity(), Forecast5Days.class);
            String searchLocation = etSearch.getText().toString().trim();
            if (!searchLocation.isEmpty()) {
                intent.putExtra("SEARCH_LOCATION", searchLocation); // Pass search location
            }

            startActivity(intent);
        });

        rvHourlyForecast = view.findViewById(R.id.rvHourlyForecast);
        rvHourlyForecast.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL,false));

        // Get today's date and the day of the week
        String currentDate = getCurrentDate();

        // Set the text for tvDate (Day of the week, Date)
        tvDate.setText(currentDate);

        // Initialize Retrofit client and location services
        weatherAPI = RetrofitClient.getClient().create(WeatherAPI.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Set up search functionality
        setupSearch();

        // Request location or fallback to default
        checkAndRequestLocationPermission();


        return view;
    }
    private void setupSearch() {
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String location = etSearch.getText().toString().trim();

                if (!location.isEmpty()) {
                    // If the location seems to be just a country (without commas), append a default city
                    if (!location.contains(",")) {
                        location = location + ", " + "capital";  // Replace "capital" with your default city (e.g., "Paris")
                    }

                    isUsingSearchLocation = true; // Enable search mode
                    fetchWeather(location, null, null); // Fetch weather for the searched location
                    btnFavorites.setImageResource(R.drawable.ic_add_favorites);
                } else {
                    Toast.makeText(getContext(), "Enter a location to search", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
    }



    private String getCurrentDate() {
        // Get current date and day of the week
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }


    private void checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission if not granted
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, fetch location
            fetchUserLocation();
        }
    }

    private void fetchUserLocation() {
        if (isUsingSearchLocation) {
            return; // Do nothing if a search location is being used
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            fetchWeather(null, latitude, longitude); // Fetch current location weather
                        } else {
                            Toast.makeText(getContext(), "Unable to fetch location, using default", Toast.LENGTH_SHORT).show();
                            fetchWeather("Manila", null, null); // Fallback to default location
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        fetchWeather("Manila", null, null); // Fallback to default location
                    });
        } else {
            Toast.makeText(getContext(), "Location permission required for current location", Toast.LENGTH_SHORT).show();
            fetchWeather("Manila", null, null); // Fallback to default location
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isUsingSearchLocation = false; // Reset search mode on pause
    }

    private void fetchWeather(String location, Double latitude, Double longitude) {
        String apiKey = "e9318361aafd38c09a045fb62117cb02";
        Call<WeatherResponse> call;

        if (location != null && !location.isEmpty()) {
            // Fetch weather by city name
            call = weatherAPI.getWeather(location, apiKey, "metric");
        } else if (latitude != null && longitude != null) {
            // Fetch weather by coordinates
            call = weatherAPI.getWeatherByCoordinates(latitude, longitude, apiKey, "metric");
        } else {
            Toast.makeText(getContext(), "Invalid location input", Toast.LENGTH_SHORT).show();
            return;
        }

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                } else {
                    Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Toast.makeText(getContext(), "API call failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void updateUI(WeatherResponse weatherResponse) {
        // Get city name and country code
        String cityName = weatherResponse.city.name;
        String countryCode = weatherResponse.city.country; // Assuming OpenWeather provides the country code

        // Set the location with both city and country
        tvLocation.setText(String.format("%s, %s", cityName.toUpperCase(), countryCode.toUpperCase()));
        tvTemperature.setText(String.format("%d°C", Math.round(weatherResponse.list.get(0).main.temp)));
        tvWeatherDescription.setText(weatherResponse.list.get(0).weather.get(0).description.toUpperCase());
        tvHumidity.setText(String.format("%s%%", weatherResponse.list.get(0).main.humidity));
        tvWindSpeed.setText(String.format("%s km/h", weatherResponse.list.get(0).wind.speed));
        tvFeelsLike.setText(String.format("Feels like %s°C", Math.round(weatherResponse.list.get(0).main.feels_like)));

        // Check for weather icon and set it
        String iconCode = weatherResponse.list.get(0).weather.get(0).icon;
        if (iconCode != null && !iconCode.isEmpty()) {
            ivWeatherIcon.setImageResource(getWeatherIcon(iconCode));  // Set the appropriate icon
        } else {
            ivWeatherIcon.setImageResource(R.drawable.ic_sunny); // Fallback icon if iconCode is missing or invalid
        }

        String precipitation = "0%"; // Default message if no precipitation
        if (weatherResponse.list.get(0).rain != null) {
            precipitation = String.format("%.1f mm", weatherResponse.list.get(0).rain.threeHourRain); // Rain in mm
            weatherIndicator.setText("Rain");
        } else if (weatherResponse.list.get(0).snow != null) {
            precipitation = String.format("%.1f mm", weatherResponse.list.get(0).snow.threeHourSnow); // Snow in mm
            weatherIndicator.setText("Snow");
        }

        // Filter hourly forecast for today only
        List<WeatherResponse.WeatherList> todayHourlyForecast = filterHourlyForecastForToday(weatherResponse.list);


        // Set precipitation data in tvRainPercentage
        tvRainPercentage.setText(precipitation);

        adapter = new HourlyForecastAdapter(todayHourlyForecast);  // Pass only today's data to the adapter
        rvHourlyForecast.setAdapter(adapter);


    }

    // Helper method to filter hourly forecast for today
    private List<WeatherResponse.WeatherList> filterHourlyForecastForToday(List<WeatherResponse.WeatherList> forecastList) {
        List<WeatherResponse.WeatherList> todayForecast = new ArrayList<>();

        // Get today's date in "yyyy-MM-dd" format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = sdf.format(new Date());

        // Filter forecasts for today
        for (WeatherResponse.WeatherList forecast : forecastList) {
            String forecastDate = forecast.dt_txt.split(" ")[0];  // Extract the date part of dt_txt
            if (forecastDate.equals(todayDate)) {
                todayForecast.add(forecast);
            }
        }

        return todayForecast;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchUserLocation();
            } else {
                Toast.makeText(getContext(), "Permission denied, using default location", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
