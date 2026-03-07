package ca.mohawk.medichelper.ui.checklist;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import ca.mohawk.medichelper.api.ApiService;
import data_models.Reminder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChecklistViewModel extends ViewModel {

    private final MutableLiveData<List<Reminder>> reminders = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private ApiService apiService;

    public void setApiService(ApiService apiService) {
        this.apiService = apiService;
    }

    public LiveData<List<Reminder>> getReminders() {
        return reminders;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void fetchReminders(String token) {
        isLoading.setValue(true);
        apiService.getReminders("Bearer " + token).enqueue(new Callback<List<Reminder>>() {
            @Override
            public void onResponse(Call<List<Reminder>> call, Response<List<Reminder>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    Log.d("ChecklistViewModel", "Reminders fetched successfully");
                    reminders.setValue(response.body());
                } else {
                    Log.e("ChecklistViewModel", "Failed to fetch reminders");
                }
            }

            @Override
            public void onFailure(Call<List<Reminder>> call, Throwable t) {
                isLoading.setValue(false);
                Log.e("ChecklistViewModel", "Network error", t);
            }
        });
    }

    public void updateMedicationTaken(int reminderId, boolean medicationTaken, String token, int dosage) {
        isLoading.setValue(true);
        Reminder reminder = new Reminder(reminderId, medicationTaken, dosage);
        apiService.updateReminder("Bearer " + token, reminder).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.setValue(false);
                if (!response.isSuccessful()) {
                    Log.e("ChecklistViewModel", "Failed to update medication taken");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.setValue(false);
                Log.e("ChecklistViewModel", "Network error", t);
            }
        });
    }

    public void deleteReminder(int reminderId, String token) {
        isLoading.setValue(true);
        apiService.deleteReminder("Bearer " + token, reminderId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.setValue(false);
                if (!response.isSuccessful()) {
                    Log.e("ChecklistViewModel", "Failed to delete reminder");
                } else {
                    fetchReminders(token); // Refresh reminders after deletion
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.setValue(false);
                Log.e("ChecklistViewModel", "Network error", t);
            }
        });
    }
}
