package ca.mohawk.medichelper.ui.register;

import static android.app.PendingIntent.getActivity;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;

import ca.mohawk.medichelper.api.ApiClient;
import ca.mohawk.medichelper.api.ApiService;
import data_models.AuthResponse;
import data_models.RegisterRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterViewModel extends ViewModel {

    private final MutableLiveData<AuthResponse> registrationResponse;
    private final ApiService apiService;

    public RegisterViewModel() {
        registrationResponse = new MutableLiveData<>();
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public LiveData<AuthResponse> getRegistrationResponse() {
        return registrationResponse;
    }

    public void registerUser(RegisterRequest registerRequest) {
        if(registerRequest != null) {
            Log.d("RegisterViewModel", "Registering user: " + registerRequest);
        }

        Call<AuthResponse> call = apiService.registerUser(registerRequest);
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful()) {
                    AuthResponse authResponse = response.body();
                    if (authResponse != null) {
                        Log.d("RegisterViewModel", "Registration successful ");
                        registrationResponse.postValue(authResponse);
                    }
                } else {

                    Log.d("RegisterViewModel", "Registration failed with status code: " + response.code());
                    try {
                        Log.d("RegisterViewModel", "Error: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    registrationResponse.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Log.d("RegisterViewModel", "Registration failed: " + t.getMessage());
                t.printStackTrace();
                registrationResponse.postValue(null);
            }
        });
    }


}
