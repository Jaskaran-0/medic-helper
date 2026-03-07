package ca.mohawk.medichelper.ui.appointments;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import ca.mohawk.medichelper.api.ApiClient;
import ca.mohawk.medichelper.api.ApiService;
import data_models.Appointment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddAppointmentViewModel extends AndroidViewModel {

    private static final String TAG = "AddAppointmentViewModel";
    private final ApiService apiService;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAppointmentAdded = new MutableLiveData<>();

    public AddAppointmentViewModel(@NonNull Application application) {
        super(application);
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<Boolean> isAppointmentAdded() {
        return isAppointmentAdded;
    }

    public void addAppointment(String token, String title, String description, String date, String time) {
        isLoading.setValue(true);

        Appointment appointment = new Appointment(title, description, date, time);

        apiService.addAppointment("Bearer " + token, appointment).enqueue(new Callback<Appointment>() {
            @Override
            public void onResponse(Call<Appointment> call, Response<Appointment> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    isAppointmentAdded.setValue(true);
                    Log.d(TAG, "Appointment added successfully");
                } else {
                    isAppointmentAdded.setValue(false);
                    Log.e(TAG, "Failed to add appointment. Response code: " + response.code());
                    Log.e(TAG, "Response message: " + response.message());

                    Toast.makeText(getApplication(), "Failed to add appointment", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Appointment> call, Throwable t) {
                isLoading.setValue(false);
                isAppointmentAdded.setValue(false);
                Log.e(TAG, "Error adding appointment", t);
                Toast.makeText(getApplication(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
