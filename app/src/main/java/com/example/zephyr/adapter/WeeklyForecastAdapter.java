package com.example.zephyr.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zephyr.R;
import com.example.zephyr.api.model.WeatherResponse;
import com.example.zephyr.utils.WeatherUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeeklyForecastAdapter extends RecyclerView.Adapter<WeeklyForecastAdapter.WeeklyForecastViewHolder> {
    private List<WeatherResponse.WeatherList> forecasts = new ArrayList<>();

    public void setData(List<WeatherResponse.WeatherList> newForecasts) {
        forecasts.clear();
        forecasts.addAll(newForecasts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WeeklyForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weekly_forecast, parent, false);
        return new WeeklyForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeeklyForecastViewHolder holder, int position) {
        WeatherResponse.WeatherList forecast = forecasts.get(position);

        // Use WeatherUtils to get the icon
        holder.ivWeatherIcon.setImageResource(WeatherUtils.getWeatherIcon(forecast.weather.get(0).icon));
        holder.tvDay.setText(getDayOfWeek(forecast.dt_txt));
        holder.tvDescription.setText(forecast.weather.get(0).description.toUpperCase());
        holder.tvMaxTemp.setText(String.format("%s°C", Math.round(forecast.main.temp)));
    }

    @Override
    public int getItemCount() {
        return forecasts.size();
    }

    static class WeeklyForecastViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvDescription, tvMaxTemp, tvMinTemp;
        ImageView ivWeatherIcon;

        public WeeklyForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            ivWeatherIcon = itemView.findViewById(R.id.ivWeatherIcon);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvMaxTemp = itemView.findViewById(R.id.tvMaxTemp);
        }
    }

    private String getDayOfWeek(String dt_txt) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        try {
            Date date = inputFormat.parse(dt_txt);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }
}
