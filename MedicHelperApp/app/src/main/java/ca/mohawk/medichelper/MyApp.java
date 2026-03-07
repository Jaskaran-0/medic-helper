package ca.mohawk.medichelper;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import ca.mohawk.medichelper.api.ApiClient;
import ca.mohawk.medichelper.api.ApiService;
import ca.mohawk.medichelper.utils.NetworkChangeReceiver;
import ca.mohawk.medichelper.utils.NetworkUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyApp extends Application {
    private static final String TAG = "FCM";
    private ApiService apiService;
    private Activity currentActivity;
    private AlertDialog noInternetDialog;
    private NetworkChangeReceiver networkChangeReceiver;

    private final Object activityMonitor = new Object();

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        apiService = ApiClient.getClient().create(ApiService.class);

        // Register network change receiver
        networkChangeReceiver = new NetworkChangeReceiver(this::onNetworkStatusChanged);
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // Get the FCM token
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                return;
            }

            String token = task.getResult();
            getSharedPreferences("UserPreferences", MODE_PRIVATE).edit().putString("fcm_token", token).apply();
            Log.d(TAG, "Token stored in SharedPreferences");

            // Only send the token if a JWT token is available
            if (getJwtToken() != null) {
                sendTokenToBackend(token);
            }
        });
    }

    public void sendTokenToBackend(String fcmToken) {
        String jwtToken = getJwtToken();
        if (jwtToken == null) {
            Log.w(TAG, "JWT token not found, unable to send FCM token to backend");
            return;
        }

        apiService.updateFCMToken("Bearer " + jwtToken, fcmToken).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "FCM token successfully sent to backend");
                } else {
                    Log.e(TAG, "Failed to send FCM token to backend, response code: " + response.code());
                    Log.e(TAG, "Response message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Failed to send FCM token to backend", t);
            }
        });
    }

    private String getJwtToken() {
        return getSharedPreferences("UserPreferences", MODE_PRIVATE).getString("jwt_token", null);
    }

    public void setCurrentActivity(Activity activity) {
        synchronized (activityMonitor) {
            this.currentActivity = activity;
            activityMonitor.notifyAll();
        }
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    public void onNetworkStatusChanged(boolean isConnected) {
        if (isConnected) {
            Log.d(TAG, "Internet connected");
            dismissNoInternetDialog();
        } else {
            Log.d(TAG, "No internet connection");
            showNoInternetDialog();
        }
    }

    private void showNoInternetDialog() {
        new Thread(() -> {
            synchronized (activityMonitor) {
                while (currentActivity == null) {
                    try {
                        activityMonitor.wait(); // Wait for an activity to be set
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Activity monitor interrupted", e);
                    }
                }
            }

//            if (noInternetDialog != null && noInternetDialog.isShowing()) {
//                return;
//            }

            if (currentActivity == null || !isAppInForeground()) {
                Log.e(TAG, "Cannot show dialog: No current activity or app not in foreground");
                return;
            }

            new Handler(Looper.getMainLooper()).post(() -> {
                noInternetDialog = new AlertDialog.Builder(currentActivity)
                        .setTitle("No Internet")
                        .setMessage("Please connect to the internet to continue.")
                        .setCancelable(false)
                        .setPositiveButton("Retry", (dialog, which) -> {
                            // Retry logic to check network status again
                            boolean isConnected = isNetworkConnected();

                            Log.d(TAG, "Network connected: " + isConnected);

                            if (!isConnected) {
                                showNoInternetDialog(); // Show dialog again if still disconnected
                            } else {
                                dismissNoInternetDialog(); // Dismiss if connected
                            }
                        })
                        .create();

                noInternetDialog.show();
            });
        }).start();
    }

    private void dismissNoInternetDialog() {
        if (noInternetDialog != null && noInternetDialog.isShowing()) {
            noInternetDialog.dismiss();
            noInternetDialog = null;
        }
    }

    private boolean isAppInForeground() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) return false;

        String packageName = getPackageName();
        for (ActivityManager.RunningAppProcessInfo processInfo : activityManager.getRunningAppProcesses()) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && processInfo.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            boolean isConnected = NetworkUtils.isInternetAvailable(this);

            return isConnected;
        }
        return false;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(networkChangeReceiver);
    }
}
