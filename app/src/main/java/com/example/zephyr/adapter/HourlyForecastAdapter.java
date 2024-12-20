package com.example.zephyr.adapter;

import static com.example.zephyr.utils.WeatherUtils.getWeatherIcon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zephyr.R;
import com.example.zephyr.api.model.WeatherResponse;

import java.util.List;

public class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.HourlyViewHolder> {

    private final List<WeatherResponse.WeatherList> hourlyForecast;

    public HourlyForecastAdapter(List<WeatherResponse.WeatherList> hourlyForecast) {
        this.hourlyForecast = hourlyForecast;
    }

    @NonNull
    @Override
    public HourlyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hourly_forecast, parent, false);
        return new HourlyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourlyViewHolder holder, int position) {
        WeatherResponse.WeatherList forecast = hourlyForecast.get(position);

        // Format and display the time
        String time = forecast.dt_txt.split(" ")[1];
        holder.tvTime.setText(time);

        // Display temperature and icon
        holder.tvTemperature.setText(String.format("%s°C", Math.round(forecast.main.temp)));
        holder.ivWeatherIcon.setImageResource(getWeatherIcon(forecast.weather.get(0).icon)); // Call utility method
    }

    @Override
    public int getItemCount() {
        return hourlyForecast.size();
    }

    public static class HourlyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvTemperature;
        ImageView ivWeatherIcon;

        public HourlyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTemperature = itemView.findViewById(R.id.tvTemperature);
            ivWeatherIcon = itemView.findViewById(R.id.ivWeatherIcon);
        }
    }
}


