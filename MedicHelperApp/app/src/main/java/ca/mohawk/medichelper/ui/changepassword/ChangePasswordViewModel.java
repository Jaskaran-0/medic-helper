package ca.mohawk.medichelper.ui.changepassword;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import ca.mohawk.medichelper.api.ApiClient;
import ca.mohawk.medichelper.api.ApiService;
import data_models.ChangePasswordDTO;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordViewModel extends AndroidViewModel {

    private final ApiService apiService;
    private final MutableLiveData<String> changePasswordResponse;
    private final Application app;

    public ChangePasswordViewModel(Application application) {
        super(application);
        apiService = ApiClient.getClient().create(ApiService.class);
        changePasswordResponse = new MutableLiveData<>();
        app = application;
    }

    public MutableLiveData<String> getChangePasswordResponse() {
        return changePasswordResponse;
    }

    public void changePassword(String email, String currentPassword, String newPassword, String confirmNewPassword, boolean fingerprintEnabled, String token) {

        // Validate input
        if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmNewPassword)) {
            Toast.makeText(app.getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            Toast.makeText(app.getApplicationContext(), "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create ChangePasswordDTO
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO(email,passwordHasher(currentPassword),passwordHasher(newPassword), fingerprintEnabled);

        // Make API call to change password
        Call<String> call = apiService.changePassword("Bearer " + token, changePasswordDTO);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d("ChangePasswordViewModel", "Password changed successfully");

                    String message = response.body();
                    changePasswordResponse.postValue(message);
                } else {

                    Log.d("ChangePasswordViewModel", "Error changing password: " + response.code());
                    Log.d("ChangePasswordViewModel", "Error message: " + response.message());

                    if(response.code() == 400){
                        Toast.makeText(app.getApplicationContext(), "Invalid credentials", Toast.LENGTH_SHORT).show();
                    }

                    changePasswordResponse.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                changePasswordResponse.setValue("Error: " + t.getMessage());
            }
        });
    }

    private String passwordHasher(String password) {
        try {
            // Create a MessageDigest instance for SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Hash the password
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // Convert the byte array to a Base64 encoded string
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
