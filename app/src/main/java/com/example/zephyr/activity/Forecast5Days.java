package com.example.zephyr.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zephyr.R;
import com.example.zephyr.adapter.WeeklyForecastAdapter;
import com.example.zephyr.api.RetrofitClient;
import com.example.zephyr.api.WeatherAPI;
import com.example.zephyr.api.model.WeatherResponse;
import com.example.zephyr.utils.WeatherUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Forecast5Days extends AppCompatActivity {
    private ImageView weatherIconWeekly;
    private ImageButton backBtn;
    private TextView tvTemperatureWeekly, tvWeatherDescriptionWeekly, tvRainPercentageWeekly, tvWindSpeedWeekly, tvHumidityWeekly;
    private RecyclerView rvWeeklyForecast;
    private WeeklyForecastAdapter weeklyForecastAdapter;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forecast_5_days);

        // Initialize views
        weatherIconWeekly = findViewById(R.id.weatherIconWeekly);
        tvTemperatureWeekly = findViewById(R.id.tvTemperatureWeekly);
        tvWeatherDescriptionWeekly = findViewById(R.id.tvWeatherDescriptionWeekly);
        tvRainPercentageWeekly = findViewById(R.id.tvRainPercentageWeekly);
        tvWindSpeedWeekly = findViewById(R.id.tvWindSpeedWeekly);
        tvHumidityWeekly = findViewById(R.id.tvHumidityWeekly);
        rvWeeklyForecast = findViewById(R.id.rvWeeklyForecast);

        // Initialize the back button
        backBtn = findViewById(R.id.btnBack);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Forecast5Days.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        rvWeeklyForecast.setLayoutManager(new LinearLayoutManager(this));
        weeklyForecastAdapter = new WeeklyForecastAdapter();
        rvWeeklyForecast.setAdapter(weeklyForecastAdapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check if a search location was passed
        String searchLocation = getIntent().getStringExtra("SEARCH_LOCATION");
        if (searchLocation != null && !searchLocation.isEmpty()) {
            // Use the search location temporarily
            fetchWeeklyForecast(searchLocation, null, null);
        } else {
            // Check location permissions and fetch data
            checkAndRequestLocationPermission();
            getCurrentLocation();
        }
}

    private void checkAndRequestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            getCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permissions if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                fetchWeeklyForecast(null, latitude, longitude); // Fetch data for current location
            } else {
                Toast.makeText(Forecast5Days.this, "Unable to fetch location, using default fallback", Toast.LENGTH_SHORT).show();
                fetchWeeklyForecast("Manila", null, null); // Fallback to Manila
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(Forecast5Days.this, "Error fetching location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            fetchWeeklyForecast("Manila", null, null); // Fallback to Manila
        });
    }



    private void fetchWeeklyForecast(String location, Double latitude, Double longitude) {
        String apiKey = "e9318361aafd38c09a045fb62117cb02"; // Replace with your OpenWeather API key
        WeatherAPI weatherAPI = RetrofitClient.getClient().create(WeatherAPI.class); // Create an instance of WeatherAPI
        Call<WeatherResponse> call;

        if (location != null && !location.isEmpty()) {
            // Fetch forecast by city name (temporary search location)
            call = weatherAPI.getWeather(location, apiKey, "metric");
        } else if (latitude != null && longitude != null) {
            // Fetch forecast by coordinates (default current location)
            call = weatherAPI.getWeatherByCoordinates(latitude, longitude, apiKey, "metric");
        } else {
            Toast.makeText(this, "Invalid location input", Toast.LENGTH_SHORT).show();
            return;
        }

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayForecast(response.body());
                } else {
                    Toast.makeText(Forecast5Days.this, "Failed to fetch weekly forecast", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Toast.makeText(Forecast5Days.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }



    private void displayForecast(WeatherResponse weatherResponse) {
        List<WeatherResponse.WeatherList> forecastList = weatherResponse.list;

        // Filter tomorrow's forecast
        List<WeatherResponse.WeatherList> tomorrowForecasts = filterForecastByDay(forecastList, 1);
        WeatherResponse.WeatherList tomorrow = tomorrowForecasts.get(0);

        // Display tomorrow's data
        weatherIconWeekly.setImageResource(WeatherUtils.getWeatherIcon(tomorrow.weather.get(0).icon));
        tvTemperatureWeekly.setText(String.format("%s°C", Math.round(tomorrow.main.temp)));
        tvWeatherDescriptionWeekly.setText(tomorrow.weather.get(0).description.toUpperCase());
        tvRainPercentageWeekly.setText(String.format("%s%%", (tomorrow.rain != null ? tomorrow.rain.threeHourRain : 0)));
        tvWindSpeedWeekly.setText(String.format("%s km/h", tomorrow.wind.speed));
        tvHumidityWeekly.setText(String.format("%s%%", tomorrow.main.humidity));

        // Filter next 3 days and set to RecyclerView
        List<WeatherResponse.WeatherList> weeklyForecasts = filterForecastByDay(forecastList, 2, 3, 4);
        weeklyForecastAdapter.setData(weeklyForecasts);
    }

    private List<WeatherResponse.WeatherList> filterForecastByDay(List<WeatherResponse.WeatherList> forecastList, int... dayOffsets) {
        List<WeatherResponse.WeatherList> filteredForecasts = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        for (int dayOffset : dayOffsets) {
            calendar.add(Calendar.DAY_OF_YEAR, dayOffset);
            String targetDate = sdf.format(calendar.getTime());

            for (WeatherResponse.WeatherList forecast : forecastList) {
                if (forecast.dt_txt.startsWith(targetDate)) {
                    filteredForecasts.add(forecast);
                    break; // Add only the first forecast of the day
                }
            }
            calendar.add(Calendar.DAY_OF_YEAR, -dayOffset); // Reset calendar
        }
        return filteredForecasts;
    }
}
