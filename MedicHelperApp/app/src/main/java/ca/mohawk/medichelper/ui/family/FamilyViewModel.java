package ca.mohawk.medichelper.ui.family;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import ca.mohawk.medichelper.api.ApiClient;
import ca.mohawk.medichelper.api.ApiService;
import data_models.FamilyMember;
import data_models.PendingRequest;
import data_models.SwitchAccountResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FamilyViewModel extends ViewModel {

    private final MutableLiveData<List<FamilyMember>> familyMembers = new MutableLiveData<>();
    private final MutableLiveData<List<PendingRequest>> pendingRequests = new MutableLiveData<>();
    private final ApiService apiService;
    private String jwtToken;

    private final MutableLiveData<SwitchAccountResponse> resp = new MutableLiveData<>();

    public FamilyViewModel() {
        apiService = ApiClient.getClient().create(ApiService.class);
        Log.d("FamilyViewModel", "Initialized FamilyViewModel");
    }

    public void setJwtToken(String token) {
        this.jwtToken = token;
        Log.d("FamilyViewModel", "JWT Token set");
    }

    public LiveData<List<FamilyMember>> getFamilyMembers() {
        return familyMembers;
    }

    public LiveData<List<PendingRequest>> getPendingRequests() {
        return pendingRequests;
    }

    public LiveData<SwitchAccountResponse> getNewJwtToken() {
        return resp;
    }

    public void fetchFamilyData() {
        if (jwtToken == null) {
            Log.e("FamilyViewModel", "JWT token is null. Cannot fetch family data.");
            return;
        }

        apiService.getFamilyMembers("Bearer " + jwtToken).enqueue(new Callback<List<FamilyMember>>() {
            @Override
            public void onResponse(Call<List<FamilyMember>> call, Response<List<FamilyMember>> response) {
                if (response.isSuccessful()) {
                    familyMembers.setValue(response.body());
                } else {
                    Log.e("FamilyViewModel", "Failed to fetch family members: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<FamilyMember>> call, Throwable t) {
                Log.e("FamilyViewModel", "Error fetching family members", t);
            }
        });

        apiService.getPendingRequests("Bearer " + jwtToken).enqueue(new Callback<List<PendingRequest>>() {
            @Override
            public void onResponse(Call<List<PendingRequest>> call, Response<List<PendingRequest>> response) {
                if (response.isSuccessful()) {
                    pendingRequests.setValue(response.body());
                } else {
                    Log.e("FamilyViewModel", "Failed to fetch pending requests: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<PendingRequest>> call, Throwable t) {
                Log.e("FamilyViewModel", "Error fetching pending requests", t);
            }
        });
    }

    public void approvePendingRequest(PendingRequest request) {
        if (jwtToken == null) {
            Log.e("FamilyViewModel", "JWT token is null. Cannot approve pending request.");
            return;
        }

        apiService.approveFamilyRequest("Bearer " + jwtToken, request.getFamilyMemberId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("FamilyViewModel", "Request approved successfully");
                    fetchFamilyData();
                } else {
                    Log.e("FamilyViewModel", "Failed to approve request: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("FamilyViewModel", "Error approving request", t);
            }
        });
    }

    public void rejectPendingRequest(PendingRequest request) {
        if (jwtToken == null) {
            Log.e("FamilyViewModel", "JWT token is null. Cannot reject pending request.");
            return;
        }

        apiService.rejectFamilyRequest("Bearer " + jwtToken, request.getFamilyMemberId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("FamilyViewModel", "Request rejected successfully: " + response.body());
                    fetchFamilyData();
                } else {
                    Log.e("FamilyViewModel", "Failed to reject request: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("FamilyViewModel", "Error rejecting request", t);
            }
        });
    }

    public void switchToFamilyAccount(String familyUserId) {
        if (jwtToken == null) {
            Log.e("FamilyViewModel", "JWT token is null. Cannot switch accounts.");
            return;
        }

        if (familyUserId == null) {
            Log.e("FamilyViewModel", "Family member ID is null. Cannot switch accounts.");
            return;
        }

        Log.d("FamilyViewModel", "Switching to family member account: " + familyUserId);

        apiService.switchToFamilyMember("Bearer " + jwtToken, familyUserId).enqueue(new Callback<SwitchAccountResponse>() {
            @Override
            public void onResponse(Call<SwitchAccountResponse> call, Response<SwitchAccountResponse> response) {
                if (response.isSuccessful()) {
                    SwitchAccountResponse rsp= response.body();

                    Log.d("FamilyViewModel", "JWT token: " + rsp.getToken());
                    Log.d("FamilyViewModel", "FCM token: " + rsp.getToken1());

                    resp.setValue(rsp);

                } else {
                    Log.e("FamilyViewModel", "Response code: " + response.code());
                    Log.e("FamilyViewModel", "Response message: " + response.message());
                    Log.e("FamilyViewModel", "Failed to switch to family member account: " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<SwitchAccountResponse> call, Throwable t) {
                Log.e("FamilyViewModel", "Error switching account", t);
            }
        });
    }

    public void removeFamilyMember(String familyMemberId) {
        if (jwtToken == null) {
            Log.e("FamilyViewModel", "JWT token is null. Cannot remove family member.");
            return;
        }

        apiService.removeFamilyMember("Bearer " + jwtToken, Integer.parseInt(familyMemberId)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("FamilyViewModel", "Family member removed successfully");
                    fetchFamilyData();
                } else {
                    Log.e("FamilyViewModel", "Failed to remove family member: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("FamilyViewModel", "Error removing family member", t);
            }
        });
    }


}
