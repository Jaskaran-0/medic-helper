package ca.mohawk.medichelper.ui.account;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.Date;

import ca.mohawk.medichelper.UserSessionManager;
import ca.mohawk.medichelper.api.ApiClient;
import ca.mohawk.medichelper.api.ApiService;

import data_models.UpdateUserDTO;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountViewModel extends AndroidViewModel {

    private static final String TAG = "AccountViewModel";
    private final ApiService apiService;
    private final UserSessionManager userSessionManager;

    // LiveData to observe the result of saving user data
    public MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();

    public AccountViewModel(Application application) {
        super(application);
        apiService = ApiClient.getClient().create(ApiService.class);
        userSessionManager = UserSessionManager.getInstance();
    }

    // Load user details from the session
    public UserSessionManager getUserSessionManager() {
        Log.d(TAG, "Fetching user details from session");
        return userSessionManager;
    }

    // Save user details and make an API call
    public void saveUserDetails(String token, String firstName, String lastName, String email, String phoneNumber) {

        Log.d(TAG, "Saving user details: " + firstName + " " + lastName + ", Email: " + email + ", Phone: " + phoneNumber);

        // Update the session locally
        userSessionManager.setUserDetails(firstName, lastName, email, phoneNumber,userSessionManager.getDob(), userSessionManager.isFingerprintEnabled());

        // Prepare the data for the API call
        UpdateUserDTO updateUserDTO = new UpdateUserDTO(firstName, lastName, email, phoneNumber);

        // Call the API to update the user's data
        Call<Void> call = apiService.updateUserDetails("Bearer " + token, updateUserDTO);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "User details updated successfully on server");
                    updateSuccess.postValue(true);
                } else {
                    Log.e(TAG, "Failed to update user details on server. Response code: " + response.code());
                    Log.e(TAG, "Response body: " + response.errorBody().toString());
                    updateSuccess.postValue(false);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error while updating user details: " + t.getMessage());
                updateSuccess.postValue(false);
            }
        });
    }

}
