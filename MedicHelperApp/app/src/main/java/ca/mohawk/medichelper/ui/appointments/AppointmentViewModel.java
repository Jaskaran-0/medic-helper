package ca.mohawk.medichelper.ui.appointments;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;

import ca.mohawk.medichelper.api.ApiClient;
import ca.mohawk.medichelper.api.ApiService;
import data_models.Appointment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentViewModel extends ViewModel {

    private final MutableLiveData<List<Appointment>> appointments = new MutableLiveData<>();
    private final MutableLiveData<Boolean> deleteStatus = new MutableLiveData<>();
    private final ApiService apiService;

    public AppointmentViewModel() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public LiveData<List<Appointment>> getAppointments() {
        return appointments;
    }

    public LiveData<Boolean> getDeleteStatus() {
        return deleteStatus;
    }

    public void resetDeleteStatus() {
        deleteStatus.setValue(null); // Reset deleteStatus after handling
    }

    public void fetchAppointments(String token) {
        apiService.getAppointments("Bearer " + token).enqueue(new Callback<List<Appointment>>() {
            @Override
            public void onResponse(Call<List<Appointment>> call, Response<List<Appointment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    appointments.setValue(response.body());
                } else {
                    Log.e("AppointmentViewModel", "Failed to fetch appointments");
                }
            }

            @Override
            public void onFailure(Call<List<Appointment>> call, Throwable t) {
                Log.e("AppointmentViewModel", "Error fetching appointments", t);
            }
        });
    }

    public void deleteAppointment(String token, int appointmentId) {
        apiService.deleteAppointment("Bearer " + token, appointmentId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    deleteStatus.setValue(true);
                } else {
                    deleteStatus.setValue(false);
                    Log.e("AppointmentViewModel", "Failed to delete appointment");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                deleteStatus.setValue(false);
                Log.e("AppointmentViewModel", "Error deleting appointment", t);
            }
        });
    }
}
