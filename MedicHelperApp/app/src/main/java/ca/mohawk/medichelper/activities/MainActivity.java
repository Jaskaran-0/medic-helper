package ca.mohawk.medichelper.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import ca.mohawk.medichelper.MyApp;
import ca.mohawk.medichelper.R;
import ca.mohawk.medichelper.UserSessionManager;
import ca.mohawk.medichelper.api.ApiClient;
import ca.mohawk.medichelper.api.ApiService;
import ca.mohawk.medichelper.databinding.ActivityMainBinding;
import data_models.SwitchAccountResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_checklist, R.id.nav_inventory, R.id.nav_appointments, R.id.nav_notes)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Update navigation header with user data
        updateNavigationHeader();
    }

    private void updateNavigationHeader() {
        UserSessionManager userData = UserSessionManager.getInstance();

        // Get the header view and set the user's name and email
        NavigationView navigationView = binding.navView;
        View headerView = navigationView.getHeaderView(0);

        TextView nameTextView = headerView.findViewById(R.id.user_name); // Name TextView ID
        TextView emailTextView = headerView.findViewById(R.id.user_email); // Email TextView ID

        nameTextView.setText(userData.getFirstName()+" " + userData.getLastName());
        emailTextView.setText(userData.getEmail());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_account) {
            openAccount();
            return true;
        } else if (id == R.id.action_change_password) {
            changePassword();
            return true;
        } else if (id == R.id.action_family) {
            openFamilyMembers();
            return true;
        } else if (id == R.id.action_logout) {
            logout(null);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    private void openAccount() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.nav_account);
    }

    private void changePassword() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.nav_change_password);
    }

    private void openFamilyMembers() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.nav_family);
    }

    public void logout(SwitchAccountResponse resp) {
        Log.d("MainActivity", "Logging out...");

        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();

        FirebaseMessaging.getInstance().deleteToken()
                .addOnCompleteListener(task -> {
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);

                    if (task.isSuccessful()) {
                        String fcmToken = sharedPreferences.getString("fcm_token", null);
                        String jwtToken = sharedPreferences.getString("jwt_token", null);
                        boolean bypass= sharedPreferences.getBoolean("bypass",false);

                        if (fcmToken != null && jwtToken != null && resp == null) {
                            if(!bypass) {
                                removeTokenFromBackend(jwtToken, fcmToken);
                                Log.d("MainActivity", "FCM token deleted successfully from backend on logout");
                            }
                        }
                    } else {
                        Log.w("MainActivity", "Failed to delete FCM token on logout", task.getException());
                    }

                    // Keep the new JWT token if provided
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (resp != null) {
                        Log.d("MainActivity", "bypass logout");

                        editor.putString("jwt_token", resp.getToken());
                        editor.putString("fcm_token", resp.getToken1());
                        editor.putBoolean("bypass", true);
                    } else {
                        editor.remove("jwt_token");
                        editor.remove("fcm_token");
                        editor.putBoolean("bypass", false);
                        Log.d("MainActivity", "normal logout");
                    }
                    editor.apply();

                    // Navigate to LoginActivity
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);


                    finish(); // Close the MainActivity

                });
    }


    private void removeTokenFromBackend(String jwtToken, String fcmToken) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.logout("Bearer " + jwtToken, fcmToken).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d("MainActivity", "FCM token removed successfully on backend: " + response.body());
                } else {
                    Log.w("MainActivity", "Failed to remove FCM token from backend: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("MainActivity", "Error while removing FCM token from backend", t);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("MainActivity", "onStop called");

        binding = null;

        boolean isbypassed = getSharedPreferences("UserPreferences", MODE_PRIVATE).getBoolean("bypass", false);
        Log.d("MainActivity", "isbypassed: " + isbypassed);

    }

    @Override
    protected void onResume() {
        super.onResume();
        ((MyApp) getApplication()).setCurrentActivity(this);
    }

}