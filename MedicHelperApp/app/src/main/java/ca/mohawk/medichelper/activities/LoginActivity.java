package ca.mohawk.medichelper.activities;

import static android.app.PendingIntent.getActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessaging;

import ca.mohawk.medichelper.MyApp;
import ca.mohawk.medichelper.R;
import ca.mohawk.medichelper.UserSessionManager;
import ca.mohawk.medichelper.api.ApiClient;
import ca.mohawk.medichelper.api.ApiService;
import data_models.UserDTO;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 101;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apiService = ApiClient.getClient().create(ApiService.class);
        Log.d(TAG, "API Service initialized.");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            } else {
                fetchAndSendFcmToken();
            }
        } else {
            fetchAndSendFcmToken();
        }

        // Check token validity or regular login flow
        checkTokenValidityOrPromptLogin();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchAndSendFcmToken();
        }
    }

    private void fetchAndSendFcmToken() {
        String token = getSharedPreferences("UserPreferences", MODE_PRIVATE).getString("fcm_token", null);
        Log.d(TAG, "FCM token found in shared pref");

        if (token != null) {
            MyApp app = (MyApp) getApplication();
            app.sendTokenToBackend(token);
        }
    }

    private void checkTokenValidityOrPromptLogin() {
        String storedToken = getStoredToken();
        Log.d(TAG, "Stored token: " + storedToken);

        if (storedToken != null) {
            Log.d(TAG, "Validating token...");
            // Token exists, validate it and retrieve user preferences
            validateTokenAndRetrieveUserData(storedToken);
        } else {
            Log.d(TAG, "No stored token found. Redirecting to login.");
            // No token, continue with manual login
            redirectToLogin();
        }
    }

    private void validateTokenAndRetrieveUserData(String token) {
        Call<UserDTO> call = apiService.getUserData("Bearer " + token);
        call.enqueue(new Callback<UserDTO>() {
            @Override
            public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                Log.d(TAG, "API response received.");

                if (response.isSuccessful()) {
                    UserDTO user = response.body();

                    Log.d(TAG, "User data retrieved");

                    if (user != null) {
                        // Save user preferences in SharedPreferences
                        saveUserPreferences(token, user.getEmail(), user.isFingerprintEnabled());

                        UserSessionManager.getInstance().setUserDetails(
                                user.getFirstName(),
                                user.getLastName(),
                                user.getEmail(),
                                user.getPhoneNumber(),
                                user.getDob(),
                                user.isFingerprintEnabled()
                        );
                        Log.d(TAG, "User preferences saved. Fingerprint enabled: " + user.isFingerprintEnabled());

                        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
                        boolean bypass = sharedPreferences.getBoolean("bypass", false);
                        String fcmToken = sharedPreferences.getString("fcm_token",null);

                        if (bypass) {
                            Log.d("LoginActivity", "bypass login");

                            if(fcmToken == null) {
                                FirebaseMessaging.getInstance().getToken()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                String token = task.getResult();
                                                sharedPreferences.edit().putString("fcm_token", token).apply();
                                                MyApp app = (MyApp) getApplication();
                                                app.sendTokenToBackend(token);
                                                Log.d("FCM", "New FCM token generated and sent to backend on login");
                                            } else {
                                                Log.w("FCM", "Failed to get new FCM token on login", task.getException());
                                            }
                                        });
                            }
                            redirectToMainActivity();
                            return;
                        }

                        // If fingerprint is enabled, show fingerprint prompt
                        if (user.isFingerprintEnabled()) {
                            showFingerprintPrompt();
                        } else {
                            redirectToLogin();
                        }
                    }
                } else {
                    // Token invalid or expired, redirect to login
                    Toast.makeText(LoginActivity.this, "Token invalid. Please login.", Toast.LENGTH_SHORT).show();

                    //log the response code and error message
                    Log.d("LoginActivity", "Response Code: " + response.code());
                    Log.d("LoginActivity", "Error Message: " + response.message());

                    redirectToLogin();
                }
            }

            @Override
            public void onFailure(Call<UserDTO> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                redirectToLogin();
            }
        });
    }

    private void saveUserPreferences(String token, String email, boolean fingerprintEnabled) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("jwt_token", token);
        editor.putBoolean("fingerprint_enabled", fingerprintEnabled);
        editor.putString("user_email", email);
        editor.apply();
        Log.d(TAG, "User preferences saved to SharedPreferences.");
    }

    private String getStoredToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        return sharedPreferences.getString("jwt_token", null);
    }

    private void showFingerprintPrompt() {
        Log.d(TAG, "Displaying fingerprint prompt.");
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, ContextCompat.getMainExecutor(this), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.d(TAG, "Fingerprint authentication succeeded.");
                redirectToMainActivity();
            }

            @Override
            public void onAuthenticationFailed() {
                Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Fingerprint authentication failed.");
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Login with Fingerprint")
                .setDescription("Use your fingerprint to log in.")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void redirectToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void redirectToLogin() {
        Log.d(TAG, "Staying in LoginActivity for manual login.");
        //stay in loginActivity
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((MyApp) getApplication()).setCurrentActivity(this);
    }

}
