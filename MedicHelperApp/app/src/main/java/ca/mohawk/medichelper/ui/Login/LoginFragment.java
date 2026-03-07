package ca.mohawk.medichelper.ui.Login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.messaging.FirebaseMessaging;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import ca.mohawk.medichelper.MyApp;
import ca.mohawk.medichelper.UserSessionManager;
import ca.mohawk.medichelper.activities.LoginActivity;
import ca.mohawk.medichelper.activities.MainActivity;
import ca.mohawk.medichelper.R;
import ca.mohawk.medichelper.activities.RegisterActivity;
import ca.mohawk.medichelper.api.ApiClient;
import ca.mohawk.medichelper.api.ApiService;
import data_models.UserDTO;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private LoginViewModel loginViewModel;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView signUpTextView;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize UI elements
        emailEditText = view.findViewById(R.id.editTextTextEmailAddress);
        passwordEditText = view.findViewById(R.id.editTextTextPassword);
        loginButton = view.findViewById(R.id.login_button);
        signUpTextView = view.findViewById(R.id.signup_redirect_textview);

        getContext();
        sharedPreferences = getActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        String savedEmail = sharedPreferences.getString("user_email", null);
        if (savedEmail != null) {
            emailEditText.setText(savedEmail); // Auto-fill email
        }

        signUpTextView.setOnClickListener(v -> {
            Log.d("LoginFragment", "Sign Up TextView clicked");
            Intent intent = new Intent(getActivity(), RegisterActivity.class);
            startActivity(intent);
        });

        // Initialize ViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Handle login button click
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String hashedPassword = passwordHasher(password);
            if (!email.isEmpty() && !hashedPassword.isEmpty()) {
                loginViewModel.login(email, hashedPassword);
            } else {
                Toast.makeText(getActivity(), "Please enter email and password", Toast.LENGTH_SHORT).show();
            }
        });

        loginViewModel.getLoginResponse().observe(getViewLifecycleOwner(), authResponse -> {
            if (authResponse != null) {

                if(authResponse.getToken().equals("Failed with status code: 401")){
                    Toast.makeText(getActivity(), "Wrong Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(authResponse.getToken().equals("Failed with status code: 404")){
                    Toast.makeText(getActivity(), "User not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Handle successful login (e.g., save token, navigate to MainActivity)
                Toast.makeText(getActivity(), "Login successful!", Toast.LENGTH_SHORT).show();

                // Store the JWT token in SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("jwt_token", authResponse.getToken());
                editor.putString("user_email", emailEditText.getText().toString()); // Save user email
                editor.apply();

                // Retrieve user details from the backend
                retrieveUserDetails(authResponse.getToken());

            } else {
                // Handle login failure
                Toast.makeText(getActivity(), "Login failed. Try again.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void retrieveUserDetails(String token) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<UserDTO> call = apiService.getUserData("Bearer " + token);
        call.enqueue(new Callback<UserDTO>() {
            @Override
            public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                if (response.isSuccessful()) {
                    UserDTO user = response.body();
                    if (user != null) {
                        UserSessionManager.getInstance().setUserDetails(
                                user.getFirstName(),
                                user.getLastName(),
                                user.getEmail(),
                                user.getPhoneNumber(),
                                user.getDob(),
                                user.isFingerprintEnabled()
                        );
                        // Store fingerprintEnabled preference from the API
                        getContext();
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("fingerprint_enabled", user.isFingerprintEnabled());
                        editor.apply();


                        FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        String token = task.getResult();
                                        sharedPreferences.edit().putString("fcm_token", token).apply();
                                        MyApp app = (MyApp) getActivity().getApplication();
                                        app.sendTokenToBackend(token);
                                        Log.d("FCM", "New FCM token generated and sent to backend on login");
                                    } else {
                                        Log.w("FCM", "Failed to get new FCM token on login", task.getException());
                                    }
                                });

                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserDTO> call, Throwable t) {
                Toast.makeText(getActivity(), "Failed to retrieve user data.", Toast.LENGTH_SHORT).show();
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
