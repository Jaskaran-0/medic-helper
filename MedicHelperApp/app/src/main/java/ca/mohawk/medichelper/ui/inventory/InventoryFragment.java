package ca.mohawk.medichelper.ui.inventory;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ca.mohawk.medichelper.R;
import ca.mohawk.medichelper.activities.MainActivity;
import data_models.Medication;

public class InventoryFragment extends Fragment implements MedicationAdapter.OnMedicationActionListener {

    private InventoryViewModel inventoryViewModel;
    private String token;
    private RecyclerView recyclerView;
    private MedicationAdapter adapter;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);

        Log.d("InventoryFragment", "Inventory Fragment created");

        // Initialize ViewModel
        inventoryViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        // Set up RecyclerView
        recyclerView = view.findViewById(R.id.recycler_medications);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        progressBar = view.findViewById(R.id.progress_loading);

        adapter = new MedicationAdapter(new ArrayList<>(), this);  // Attach empty adapter initially
        recyclerView.setAdapter(adapter);

        // Fetch JWT token from SharedPreferences
        getContext();
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("jwt_token", null);

        if (token != null) {
            // Fetch medications from ViewModel if no data loaded yet
            if (inventoryViewModel.getMedications().getValue() == null) {
                progressBar.setVisibility(View.VISIBLE); // Show progress before loading
                inventoryViewModel.fetchMedications(token);
            }
        } else {
            Toast.makeText(getContext(), "Token not found. Please log in.", Toast.LENGTH_SHORT).show();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).logout(null);
            }
            Log.e("InventoryFragment", "Token is null; cannot fetch data");
        }

        // Observe loading state from ViewModel
        inventoryViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                Log.d("InventoryFragment", "Loading state updated: " + isLoading);
            }
        });

        // Observe medication list from ViewModel
        inventoryViewModel.getMedications().observe(getViewLifecycleOwner(), medications -> {
            if (medications == null) {
                progressBar.setVisibility(View.VISIBLE);
                return;
            }

            if (medications.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "No medications found", Toast.LENGTH_SHORT).show();
                return;
            }

            adapter.updateMedications(medications);
            progressBar.setVisibility(View.GONE);
            Log.d("InventoryFragment", "Medications updated in RecyclerView");

        });


        // Handle FAB click
        view.findViewById(R.id.fab).setOnClickListener(v -> {
            Log.d("InventoryFragment", "FAB clicked to add medication");
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_add_inventory);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("InventoryFragment", "Resuming InventoryFragment, fetching updated data");

        // Fetch updated medications list
        if (token != null) {
            inventoryViewModel.fetchMedications(token);
        } else {
            Log.e("InventoryFragment", "Token is null; cannot fetch data");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("InventoryFragment", "Inventory Fragment destroyed");
    }

    // Handle update action from adapter
    @Override
    public void onUpdateClicked(int medicationId, String name, int newInventory) {
        Log.d("InventoryFragment", "Updating medication ID: " + medicationId);
        inventoryViewModel.updateMedication(token, medicationId, name, newInventory);
    }

    // Handle delete action from adapter
    @Override
    public void onDeleteClicked(int medicationId) {
        Log.d("InventoryFragment", "Deleting medication ID: " + medicationId);
        inventoryViewModel.deleteMedication(token, medicationId);
    }
}
