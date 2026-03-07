package ca.mohawk.medichelper.ui.checklist;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.util.List;

import ca.mohawk.medichelper.api.ApiClient;
import ca.mohawk.medichelper.api.ApiService;
import data_models.Medication;
import data_models.Reminder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddReminderViewModel extends ViewModel {
    private final ApiService apiService;
    private final MutableLiveData<Boolean> isReminderAdded = new MutableLiveData<>();
    private final MutableLiveData<List<Medication>> medicationsLiveData = new MutableLiveData<>();

    public AddReminderViewModel() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public LiveData<Boolean> isReminderAdded() {
        return isReminderAdded;
    }

    public LiveData<List<Medication>> getMedicationsLiveData() {
        return medicationsLiveData;
    }

    public void fetchMedications(String token) {
        apiService.getMedications("Bearer " + token).enqueue(new Callback<List<Medication>>() {
            @Override
            public void onResponse(Call<List<Medication>> call, Response<List<Medication>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    medicationsLiveData.setValue(response.body());
                } else {
                    Log.e("AddReminderViewModel", "Failed to fetch medications: " + response.message());
                    Log.e("AddReminderViewModel", "Response Code: " + response.code());
                    Log.e("AddReminderViewModel", "Error Body: " + response.errorBody());
                    medicationsLiveData.setValue(null); // Null indicates failure
                }
            }

            @Override
            public void onFailure(Call<List<Medication>> call, Throwable t) {
                Log.e("AddReminderViewModel", "Network request failed: " + t.getMessage());
                medicationsLiveData.setValue(null);
            }
        });
    }

    public void addReminder(Reminder reminder, String token) {
        Log.d("ReminderViewModel", "Adding reminder: " + reminder.toString());
        apiService.addReminder("Bearer " + token, reminder).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    isReminderAdded.setValue(true);
                    Log.d("ReminderViewModel", "Reminder added successfully");
                } else {
                    isReminderAdded.setValue(false);
                    Log.e("ReminderViewModel", "Failed to add reminder. Code: " + response.code()
                            + ", Message: " + response.message());

                    try {
                        // Log additional details from the error body if available
                        if (response.errorBody() != null) {
                            Log.e("ReminderViewModel", "Error body: " + response.errorBody().string());
                        }
                    } catch (IOException e) {
                        Log.e("ReminderViewModel", "Failed to read error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isReminderAdded.setValue(false);
                Log.e("ReminderViewModel", "Failed to add reminder due to network error", t);
            }
        });
    }

}
