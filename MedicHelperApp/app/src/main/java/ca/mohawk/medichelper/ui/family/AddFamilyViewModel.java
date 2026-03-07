package ca.mohawk.medichelper.ui.family;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ca.mohawk.medichelper.api.ApiClient;
import ca.mohawk.medichelper.api.ApiService;
import data_models.AddFamilyMemberDTO;
import data_models.ApiResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddFamilyViewModel extends ViewModel {

    private final MutableLiveData<ApiResponse> addFamilyResponse = new MutableLiveData<>();
    private final ApiService apiService;

    public AddFamilyViewModel() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public LiveData<ApiResponse> getAddFamilyResponse() {
        return addFamilyResponse;
    }

    public void sendFamilyRequest(String token, String email) {
        if (token == null) {
            Log.e("AddFamilyViewModel", "JWT token is null. Cannot send family request.");
            return;
        }

        AddFamilyMemberDTO request = new AddFamilyMemberDTO(email);

        apiService.sendFamilyRequest("Bearer " + token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("AddFamilyViewModel", "Response received: " + response);
                if (response.isSuccessful()) {
                    Log.d("AddFamilyViewModel", "Family request sent successfully: " + response.body());
                    addFamilyResponse.setValue(new ApiResponse(true, ""));
                } else {
                    Log.d("AddFamilyViewModel", "Response body: " + response);
                    Log.d("AddFamilyViewModel", "Response code: " + response.code());
                    Log.d("AddFamilyViewModel", "Response message: " + response.message());
                    if(response.code() == 400){
                        addFamilyResponse.setValue(new ApiResponse(false, "Request is already sent"));
                    }
                    else{
                        addFamilyResponse.setValue(new ApiResponse(false, "Failed to send request: " + response.message()));
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("AddFamilyViewModel", "Error sending family request", t);
                addFamilyResponse.setValue(new ApiResponse(false, "Error: " + t.getMessage()));
            }
        });
    }
}
