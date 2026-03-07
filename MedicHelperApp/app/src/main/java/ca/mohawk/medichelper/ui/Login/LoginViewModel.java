package ca.mohawk.medichelper.ui.Login;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;

import ca.mohawk.medichelper.api.ApiClient;
import ca.mohawk.medichelper.api.ApiService;
import data_models.AuthResponse;
import data_models.LoginRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends ViewModel {
    public static final String tag = "LoginViewModel";
    private final MutableLiveData<AuthResponse> loginResponseLiveData;
    private final ApiService apiService;

    public LoginViewModel() {
        loginResponseLiveData = new MutableLiveData<>();
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public LiveData<AuthResponse> getLoginResponse() {
        return loginResponseLiveData;
    }

    public void login(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);


        apiService.login(loginRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(tag, "Success: " + response.body().toString()); // Log the response body

                    loginResponseLiveData.postValue(response.body());
                } else {
                    // Log more details about the response error
                    Log.d(tag, "Failed with status code: " + response.code());
                    try {
                        Log.d(tag, "Error: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    loginResponseLiveData.postValue(new AuthResponse("Failed with status code: " + response.code()));  // login failed
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Log.d(tag, "Network error:");
                t.printStackTrace();  // This will print the stack trace in Logcat and help you debug the error
                loginResponseLiveData.postValue(null);  // network error
            }
        });
    }
}
