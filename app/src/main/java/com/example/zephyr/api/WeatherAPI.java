    package com.example.zephyr.api;

    import com.example.zephyr.api.model.WeatherResponse;

    import retrofit2.Call;
    import retrofit2.http.GET;
    import retrofit2.http.Query;

    public interface WeatherAPI {
        @GET("forecast")
        Call<WeatherResponse> getWeatherByCoordinates(
                @Query("lat") double latitude,
                @Query("lon") double longitude,
                @Query("appid") String apiKey,
                @Query("units") String units
        );

        @GET("forecast")
        Call<WeatherResponse> getWeather(
                @Query("q") String location,
                @Query("appid") String apiKey,
                @Query("units") String units
        );
    }

