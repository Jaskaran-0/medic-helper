package ca.mohawk.medichelper.ui.inventory;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import ca.mohawk.medichelper.api.ApiClient;
import ca.mohawk.medichelper.api.ApiService;
import data_models.Medication;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ViewModel for Add Inventory screen.
 * Handles the data and business logic for adding a medication and managing image selection.
 */
public class AddInventoryViewModel extends AndroidViewModel {

    // LiveData for storing medication name, inventory count, and selected image
    private final MutableLiveData<Bitmap> selectedImage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isAdded = new MutableLiveData<>(null);
    private final MutableLiveData<Medication> addedMedication = new MutableLiveData<>();

    private Uri imageUri; // To store the selected image's URI
    private final ApiService apiService;

    public AddInventoryViewModel(Application application) {
        super(application);
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public LiveData<Medication> getAddedMedication() {
        return addedMedication;
    }

    public LiveData<Bitmap> getSelectedImage() {
        return selectedImage;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getMedicationAdded() {
        return isAdded;
    }

    public void setImage(Bitmap bitmap, Uri uri) {
        selectedImage.postValue(bitmap);
        imageUri = uri;
        Log.d("AddInventoryViewModel", "Image selected and URI set");
    }

    public Uri getImageUri() {
        return imageUri;
    }

    // Method to convert the selected image to Base64
    private String convertImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    // Method to handle adding a medication
    public void addMedication(String token, String medicationName, String inventoryCount) {
        isLoading.postValue(true);
        String base64Image = null;

        if (selectedImage.getValue() != null) {
            base64Image = convertImageToBase64(selectedImage.getValue());
        }

        if (medicationName== null || medicationName.isEmpty() || inventoryCount== null || inventoryCount.isEmpty()) {
            Log.e("AddInventoryViewModel", "Error: Fields cannot be empty");
            Log.e("AddInventoryViewModel", "Medication name: " + medicationName);
            Log.e("AddInventoryViewModel", "Inventory count: " + inventoryCount);
            isLoading.postValue(false);
            return;
        }

        // Create the medication object
        Medication medication = new Medication(medicationName, Integer.parseInt(inventoryCount), base64Image);

        // Call the API to add the medication
        Call<Medication> call = apiService.addMedication("Bearer " + token, medication);
        call.enqueue(new Callback<Medication>() {
            @Override
            public void onResponse(Call<Medication> call, Response<Medication> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    addedMedication.postValue(response.body());
                    isAdded.postValue(true);
                    Log.d("AddInventoryViewModel", "Medication added successfully: " + response.body());

                } else {
                    isAdded.postValue(false);
                    addedMedication.postValue(null);
                    Log.e("AddInventoryViewModel", "Error adding medication: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Medication> call, Throwable t) {
                isLoading.postValue(false);
                isAdded.postValue(false);
                addedMedication.postValue(null);
                Log.e("AddInventoryViewModel", "Failed to add medication", t);
            }
        });
    }
}
