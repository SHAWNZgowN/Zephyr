package com.example.zephyr.utils;

import com.example.zephyr.R;

public class WeatherUtils {
    public static int getWeatherIcon(String icon) {
        switch (icon) {
            case "01d": // Clear sky (day)
                return R.drawable.ic_sunny;
            case "02d": // Few clouds (day)
                return R.drawable.ic_sunnycloudy;
            case "02n": // Few clouds (night)
                return R.drawable.ic_cloudy;
            case "03d": // Scattered clouds
            case "03n":
                return R.drawable.ic_cloudy;
            case "04d": // Broken clouds
            case "04n":
                return R.drawable.ic_cloudy;
            case "09d": // Shower rain
            case "09n":
                return R.drawable.ic_rainshower;
            case "10d": // Rain (day)
                return R.drawable.ic_rainy;
            case "10n": // Rain (night)
                return R.drawable.ic_rainy;
            case "11d": // Thunderstorm
            case "11n":
                return R.drawable.ic_thunder;
            case "13d": // Snow
            case "13n":
                return R.drawable.ic_snowy;
            case "50d": //A Mist
            case "50n":
                return R.drawable.ic_very_cloudy;


            // Additional mappings for more granular weather conditions
            case "13d_heavy": // Heavy snow (day)
                return R.drawable.ic_heavysnow;
            case "09d_heavy": // Heavy rain shower (day)
                return R.drawable.ic_rainshower;
            case "10d_heavy": // Heavy rain (day)
                return R.drawable.ic_rainy;
            case "11d_severe": // Severe thunderstorm (day)
                return R.drawable.ic_rainythunder;

            default: // Default icon if the iconCode is unrecognized
                return R.drawable.ic_sunny; // Ensure this icon exists
        }
    }
}