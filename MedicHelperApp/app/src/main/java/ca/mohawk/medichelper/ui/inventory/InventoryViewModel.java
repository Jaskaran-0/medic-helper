package ca.mohawk.medichelper.ui.inventory;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import ca.mohawk.medichelper.api.ApiClient;
import ca.mohawk.medichelper.api.ApiService;
import data_models.Medication;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InventoryViewModel extends ViewModel {

    private final MutableLiveData<List<Medication>> medicationsLiveData =new MutableLiveData<>();;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final ApiService apiService;

    public InventoryViewModel() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public LiveData<List<Medication>> getMedications() {
        return medicationsLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void addMedicationToList(Medication newMedication) {
        List<Medication> currentList = medicationsLiveData.getValue();
        if (currentList != null) {
            currentList.add(newMedication);
            medicationsLiveData.postValue(currentList);
            Log.d("InventoryViewModel", "Medication added to list");
        }
    }

    public void fetchMedications(String token) {
        Log.d("InventoryViewModel", "Fetching medications from API");

        medicationsLiveData.setValue(null);
        apiService.getMedications("Bearer " + token).enqueue(new Callback<List<Medication>>() {
            @Override
            public void onResponse(Call<List<Medication>> call, Response<List<Medication>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    medicationsLiveData.setValue(response.body());
                    Log.d("InventoryViewModel", "Fetched " + response.body().size() + " medications");
                } else {
                    Log.e("InventoryViewModel", "Error fetching medications: " + response.message());
                    medicationsLiveData.setValue(new ArrayList<>()); // Empty list in case of failure
                }
            }

            @Override
            public void onFailure(Call<List<Medication>> call, Throwable t) {
                medicationsLiveData.setValue(new ArrayList<>()); // Empty list in case of failure
                Log.e("InventoryViewModel", "Failed to fetch medications", t);
            }
        });
    }

    // Update medication API call
    public void updateMedication(String token, int medicationId, String name, int newInventory) {
        isLoading.postValue(true);
        Medication updatedMedication = new Medication(medicationId, name, newInventory);

        apiService.updateMedication("Bearer " + token, updatedMedication).enqueue(new Callback<Medication>() {
            @Override
            public void onResponse(Call<Medication> call, Response<Medication> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) {
                    fetchMedications(token);  // Refresh the list after updating
                    Log.d("InventoryViewModel", "Medication updated successfully");
                } else {
                    Log.e("InventoryViewModel", "Error updating medication: " + response.message());
                    Log.e("InventoryViewModel", "Response code: " + response.code());
                    Log.e("InventoryViewModel", "Response body: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<Medication> call, Throwable t) {
                isLoading.postValue(false);
                Log.e("InventoryViewModel", "Failed to update medication", t);
            }
        });
    }

    // Delete medication API call
    public void deleteMedication(String token, int medicationId) {
        apiService.deleteMedication("Bearer " + token, medicationId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    fetchMedications(token);  // Refresh the list after deletion
                    Log.d("InventoryViewModel", "Medication deleted successfully");
                } else {
                    Log.e("InventoryViewModel", "Error deleting medication: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("InventoryViewModel", "Failed to delete medication", t);
            }
        });
    }
}
