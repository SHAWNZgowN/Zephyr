package com.example.zephyr.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zephyr.R;
import com.example.zephyr.api.RetrofitClient;
import com.example.zephyr.api.WeatherAPI;
import com.example.zephyr.api.model.WeatherResponse;
import com.example.zephyr.fragments.FavoritesFragment;
import com.example.zephyr.fragments.HomeFragment;
import com.example.zephyr.utils.WeatherUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder> {

    private Context context;
    private List<String> locations; // List of location names
    private List<String> documentIds; // List of document ids
    private FirebaseFirestore db;
    private WeatherAPI weatherAPI;
    private boolean areCheckboxesVisible = false; // Flag to control visibility of checkboxes
    private List<Boolean> selectedItems; // Track selected state of items
    private FavoritesFragment favoritesFragment;

    public FavoritesAdapter(Context context, List<String> locations, List<String> documentIds, FavoritesFragment favoritesFragment) {
        this.context = context;
        this.locations = locations;
        this.documentIds = documentIds;
        this.favoritesFragment = favoritesFragment;
        this.db = FirebaseFirestore.getInstance();
        this.weatherAPI = RetrofitClient.getClient().create(WeatherAPI.class);
        this.selectedItems = new ArrayList<>(locations.size());
        for (int i = 0; i < locations.size(); i++) {
            selectedItems.add(false); // Initially, no items are selected
        }
    }

    @NonNull
    @Override
    public FavoritesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_locations, parent, false);
        return new FavoritesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesViewHolder holder, int position) {
        String locationName = locations.get(position);
        holder.tvLocationName.setText(locationName);

        // Fetch weather icon using OpenWeather API
        fetchWeatherIcon(locationName, holder.ivWeatherIcon);

        // Handle checkbox visibility based on long press
        holder.checkboxSelect.setVisibility(areCheckboxesVisible ? View.VISIBLE : View.GONE);

        // Handle checkbox selection
        holder.checkboxSelect.setChecked(selectedItems.get(position));
        holder.checkboxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            selectedItems.set(position, isChecked);
            updateSelectedCount();
        });

        // Handle long press to show checkboxes
        holder.itemView.setOnLongClickListener(v -> {
            areCheckboxesVisible = true;
            favoritesFragment.showCheckboxes(); // Show checkboxes in the fragment
            notifyDataSetChanged(); // Notify adapter to update UI
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public void selectAllItems(boolean selectAll) {
        for (int i = 0; i < selectedItems.size(); i++) {
            selectedItems.set(i, selectAll); // Set the state of each item (true for selected, false for deselected)
        }
        notifyDataSetChanged(); // Notify the adapter to update the UI
    }


    public void hideCheckboxes() {
        areCheckboxesVisible = false;
        notifyDataSetChanged(); // Notify adapter to hide checkboxes
    }

    public void showCheckboxes() {
        areCheckboxesVisible = true;
        notifyDataSetChanged(); // Notify adapter to show checkboxes
    }

    private void updateSelectedCount() {
        int selectedCount = 0;
        for (Boolean isSelected : selectedItems) {
            if (isSelected) {
                selectedCount++;
            }
        }
        // Call the fragment method to update the count
        if (favoritesFragment != null) {
            favoritesFragment.updateSelectedCount(selectedCount); // Update count in Fragment
        }
    }

    public List<String> getSelectedLocations() {
        List<String> selectedLocations = new ArrayList<>();
        for (int i = 0; i < selectedItems.size(); i++) {
            if (selectedItems.get(i)) {
                selectedLocations.add(locations.get(i)); // Add selected location
            }
        }
        return selectedLocations;
    }


    public class FavoritesViewHolder extends RecyclerView.ViewHolder {

        TextView tvLocationName;
        ImageView ivWeatherIcon;
        CheckBox checkboxSelect;

        public FavoritesViewHolder(View itemView) {
            super(itemView);
            tvLocationName = itemView.findViewById(R.id.tvLocationName);
            ivWeatherIcon = itemView.findViewById(R.id.ivWeatherIcon);
            checkboxSelect = itemView.findViewById(R.id.checkboxSelect);
        }
    }

    // Fetch the weather icon for the location
    private void fetchWeatherIcon(String locationName, ImageView ivWeatherIcon) {
        String apiKey = "e9318361aafd38c09a045fb62117cb02"; // Replace with your OpenWeather API key

        // Make API call to get weather data
        weatherAPI.getWeather(locationName, apiKey, "metric").enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Get the weather icon code from the response
                    String iconCode = response.body().list.get(0).weather.get(0).icon;

                    // Use WeatherUtils to get the corresponding drawable resource ID
                    int weatherIconResId = WeatherUtils.getWeatherIcon(iconCode);

                    // Set the icon resource to the ImageView
                    ivWeatherIcon.setImageResource(weatherIconResId);
                } else {
                    ivWeatherIcon.setImageResource(R.drawable.ic_sunny); // Fallback icon
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                ivWeatherIcon.setImageResource(R.drawable.ic_sunny); // Fallback icon
                Toast.makeText(context, "Failed to load weather icon", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
