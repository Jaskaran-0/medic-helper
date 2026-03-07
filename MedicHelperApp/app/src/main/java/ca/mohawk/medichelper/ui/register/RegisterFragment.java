package ca.mohawk.medichelper.ui.register;

import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ca.mohawk.medichelper.MyApp;
import ca.mohawk.medichelper.R;

import ca.mohawk.medichelper.UserSessionManager;
import ca.mohawk.medichelper.activities.MainActivity;
import data_models.RegisterRequest;

public class RegisterFragment extends Fragment {

    private RegisterViewModel registerViewModel;
    private EditText etFirstName, etLastName, etEmail, etPhoneNumber, etPassword, etConfirmPassword;
    private DatePicker dpDob;
    private SwitchCompat switchFingerprint;
    private Button btnSignUp;
    private String fcmToken;
    private static final String TAG = "RegisterFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Initialize UI elements
        etFirstName = view.findViewById(R.id.et_first_name);
        etLastName = view.findViewById(R.id.et_last_name);
        etEmail = view.findViewById(R.id.et_email);
        etPhoneNumber = view.findViewById(R.id.et_phone_number);
        etPassword = view.findViewById(R.id.et_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        dpDob = view.findViewById(R.id.dp_dob);
        switchFingerprint = view.findViewById(R.id.switch_fingerprint);
        btnSignUp = view.findViewById(R.id.btn_sign_up);

        getContext();
        SharedPreferences prefs = getActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        fcmToken = prefs.getString("fcm_token", null);

        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        // Set up click listener for sign-up button
        btnSignUp.setOnClickListener(v -> {
            if (validateInputs()) {
                // Fetch FCM token before proceeding
                getNewFCMToken(token -> {
                    if (token != null) {
                        fcmToken = token;
                        Log.d(TAG, "Using FCM token: " + fcmToken);
                        // Proceed with registration
                        registerViewModel.registerUser(getRegistrationData());
                    } else {
                        Toast.makeText(getContext(), "Failed to generate FCM token. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        switchFingerprint.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Check if biometric authentication is supported and enrolled
                if (isBiometricSupported()) {
                    // Show biometric prompt for registration
                    showBiometricPrompt();
                } else {
                    switchFingerprint.setChecked(false);
                    Toast.makeText(getContext(), "Biometric authentication not supported or no fingerprints enrolled.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        registerViewModel.getRegistrationResponse().observe(getViewLifecycleOwner(), authResponse -> {
            if(authResponse==null)
            {
                Toast.makeText(getActivity(), "User already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            if (authResponse != null) {
                // Registration successful
                Toast.makeText(getActivity(), "Registration successful!", Toast.LENGTH_SHORT).show();

                UserSessionManager.getInstance().setUserDetails(
                        etFirstName.getText().toString(),
                        etLastName.getText().toString(),
                        etEmail.getText().toString(),
                        etPhoneNumber.getText().toString(),
                        getDateFromDatePicker2(dpDob),
                        switchFingerprint.isChecked()
                );

                // Store the JWT token in SharedPreferences
                getContext();
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("jwt_token", authResponse.getToken());
                editor.putBoolean("fingerprint_enabled", switchFingerprint.isChecked());
                editor.putString("user_email", etEmail.getText().toString());
                editor.putString("fcm_token", fcmToken);
                editor.apply();

                // Send FCM token to backend
                if (fcmToken != null) {
                    MyApp app = (MyApp) getActivity().getApplication();
                    app.sendTokenToBackend(fcmToken);
                    Log.d(TAG, "FCM token sent to backend: " + fcmToken);
                }

                // Redirect to MainActivity
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);

                getActivity().finish();
            }
        });

        return view;
    }

    private void getNewFCMToken(OnFCMTokenGeneratedListener listener) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        fcmToken = task.getResult();
                        Log.d(TAG, "New FCM token generated: " + fcmToken);
                        if (listener != null) {
                            listener.onTokenGenerated(fcmToken);
                        }
                    } else {
                        Log.w(TAG, "Failed to get new FCM token", task.getException());
                        if (listener != null) {
                            listener.onTokenGenerated(null);
                        }
                    }
                });
    }

    private boolean isBiometricSupported() {
        BiometricManager biometricManager = BiometricManager.from(getContext());
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(getContext(), "No biometric hardware found", Toast.LENGTH_SHORT).show();
                return false;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(getContext(), "Biometric hardware unavailable", Toast.LENGTH_SHORT).show();
                return false;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(getContext(), "No biometric credentials enrolled", Toast.LENGTH_SHORT).show();
                return false;
            default:
                return false;
        }
    }

    private void showBiometricPrompt() {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setDescription("Register your fingerprint to enable biometric login.")
                .setNegativeButtonText("Cancel")
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(this, ContextCompat.getMainExecutor(getContext()), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.d("ChangePasswordFragment", "Fingerprint authenticated successfully");
                Toast.makeText(getContext(), "Fingerprint authenticated successfully", Toast.LENGTH_SHORT).show();
                switchFingerprint.setChecked(true);  // Allow user to enable fingerprint login after successful authentication
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Log.d("ChangePasswordFragment", "Fingerprint authentication failed");
                Toast.makeText(getContext(), "Fingerprint authentication failed", Toast.LENGTH_SHORT).show();
                switchFingerprint.setChecked(false);  // Disable the switch if authentication fails
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.e("ChangePasswordFragment", "Authentication error: " + errString);
                Toast.makeText(getContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                switchFingerprint.setChecked(false);  // Disable the switch if an error occurs
            }
        });

        biometricPrompt.authenticate(promptInfo);
    }

    private void handleBiometricSuccess() {

        Toast.makeText(getContext(), "Fingerprint registered successfully", Toast.LENGTH_SHORT).show();
    }


    private boolean validateInputs() {
        // Validation logic

        if (TextUtils.isEmpty(etFirstName.getText())) {
            etFirstName.setError("First Name is required");
            return false;
        }

        if (TextUtils.isEmpty(etLastName.getText())) {
            etLastName.setError("Last Name is required");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString()).matches()) {
            etEmail.setError("Enter a valid email");
            return false;
        }

        String phone = etPhoneNumber.getText().toString();

// Check if the phone number length is between 10 and 12 digits
        if (phone.length() < 10 || phone.length() > 12) {
            etPhoneNumber.setError("Phone number must be between 10 and 12 digits");
            return false;
        }

// Check if the phone number contains only digits
        if (!phone.matches("\\d+")) {
            etPhoneNumber.setError("Phone number must contain only digits");
            return false;
        }

// Check if the phone number contains any spaces or special characters
        if (phone.contains(" ") || !phone.matches("[0-9]+")) {
            etPhoneNumber.setError("Phone number should not contain spaces or special characters");
            return false;
        }

// Check if the phone number starts with a valid country code (for example, +1 for US)
        if (!phone.startsWith("+") && phone.length() > 10) {
            etPhoneNumber.setError("For international numbers, ensure it starts with a valid country code");
            return false;
        }

        Calendar today = Calendar.getInstance();
        int year = dpDob.getYear();
        int age = today.get(Calendar.YEAR) - year;
        if (age < 18 || age > 120) {
            Toast.makeText(getContext(), "Date of birth must result in age between 18 and 120", Toast.LENGTH_SHORT).show();
            return false;
        }

        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();
        if (password.length() < 8) {
            etPassword.setError("Password must be at least 8 characters");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return false;
        }

        return true;
    }

    private RegisterRequest getRegistrationData() {
        return new RegisterRequest(
                etFirstName.getText().toString(),
                etLastName.getText().toString(),
                etEmail.getText().toString(),
                etPhoneNumber.getText().toString(),
                getDateFromDatePicker(dpDob),
                passwordHasher(etPassword.getText().toString()),
                switchFingerprint.isChecked(),
                fcmToken
        );
    }

    private String getDateFromDatePicker(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        Date dob = calendar.getTime();

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        return isoFormat.format(dob);
    }

    private Date getDateFromDatePicker2(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        Date dob = calendar.getTime();
        return dob;
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
    interface OnFCMTokenGeneratedListener {
        void onTokenGenerated(String token);
    }
}
