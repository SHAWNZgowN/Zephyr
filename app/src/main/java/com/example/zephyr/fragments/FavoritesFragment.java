package com.example.zephyr.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zephyr.R;
import com.example.zephyr.adapter.FavoritesAdapter;
import com.example.zephyr.api.RetrofitClient;
import com.example.zephyr.api.WeatherAPI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView recyclerView;
    private FavoritesAdapter favoritesAdapter;
    private List<String> locations; // List of location names
    private List<String> documentIds; // List of document ids
    private FirebaseFirestore db;
    private WeatherAPI weatherAPI;
    private TextView tvSelectedCount;
    private ImageButton btnCancel, btnDeleteSelected;
    private CheckBox checkboxSelectAll;
    private LinearLayout deleteForm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        // Initialize Firestore and WeatherAPI
        db = FirebaseFirestore.getInstance();
        weatherAPI = RetrofitClient.getClient().create(WeatherAPI.class);

        // Initialize UI components
        tvSelectedCount = view.findViewById(R.id.tvSelectedCount);
        btnCancel = view.findViewById(R.id.btnCancel);
        checkboxSelectAll = view.findViewById(R.id.checkboxSelectAll);
        recyclerView = view.findViewById(R.id.recyclerViewFavorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        btnDeleteSelected = view.findViewById(R.id.btnDeleteSelected);
        deleteForm = view.findViewById(R.id.deleteForm);

        checkboxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (favoritesAdapter != null) {
                favoritesAdapter.selectAllItems(isChecked);
            }
        });


        // Set the cancel button click listener
        btnCancel.setOnClickListener(v -> hideCheckboxes());

        // Set the delete button click listener for deleting selected items
        btnDeleteSelected.setOnClickListener(v -> deleteSelectedFavorites());

        // Fetch favorites from Firestore
        fetchFavorites();

        return view;
    }

    private void fetchFavorites() {
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Use UID instead of email
        db.collection("users").document(userUid).collection("favorites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    locations = new ArrayList<>();
                    documentIds = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        locations.add(document.getString("location"));
                        documentIds.add(document.getId());
                    }
                    favoritesAdapter = new FavoritesAdapter(getContext(), locations, documentIds, this); // Pass the fragment for updating UI
                    recyclerView.setAdapter(favoritesAdapter);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to fetch favorites", Toast.LENGTH_SHORT).show());
    }

    // Call this method to hide checkboxes when not in multi-select mode
    private void hideCheckboxes() {
        favoritesAdapter.hideCheckboxes(); // Hide checkboxes
        tvSelectedCount.setVisibility(View.GONE); // Hide the selected count
        btnCancel.setVisibility(View.GONE); // Hide the cancel button
        checkboxSelectAll.setVisibility(View.GONE); // Hide the select all checkbox
        deleteForm.setVisibility(View.GONE);
    }

    // Handle long press action to show checkboxes, select all checkbox, and cancel button
    public void showCheckboxes() {
        favoritesAdapter.showCheckboxes(); // Show checkboxes
        tvSelectedCount.setVisibility(View.VISIBLE); // Show selected count
        btnCancel.setVisibility(View.VISIBLE); // Show cancel button
        checkboxSelectAll.setVisibility(View.VISIBLE); // Show select all checkbox
        deleteForm.setVisibility(View.VISIBLE);
    }

    // Update the selected count TextView
    public void updateSelectedCount(int count) {
        tvSelectedCount.setText(count + " items selected");
    }

    // Delete selected favorites
    private void deleteSelectedFavorites() {
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get the current user's UID
        List<String> selectedLocations = favoritesAdapter.getSelectedLocations(); // Get selected locations from adapter

        if (selectedLocations.isEmpty()) {
            Toast.makeText(getContext(), "No items selected to delete.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Favorites")
                .setMessage("Are you sure you want to delete the selected favorites?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Proceed with deletion after confirmation
                        for (String locationName : selectedLocations) {
                            db.collection("users")
                                    .document(userUid)
                                    .collection("favorites")
                                    .document(locationName)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // Update the UI after deletion
                                        locations.remove(locationName);
                                        documentIds.remove(locationName);
                                        favoritesAdapter.notifyDataSetChanged();
                                        Toast.makeText(getContext(), "Selected locations removed from favorites", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to remove selected locations", Toast.LENGTH_SHORT).show());
                        }

                        // Hide checkboxes and update UI
                        hideCheckboxes();
                    }
                })
                .setNegativeButton("No", null) // If user cancels, just dismiss the dialog
                .show();
    }
}
