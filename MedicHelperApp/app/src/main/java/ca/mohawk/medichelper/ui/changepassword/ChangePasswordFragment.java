package ca.mohawk.medichelper.ui.changepassword;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.concurrent.Executor;

import ca.mohawk.medichelper.R;
import ca.mohawk.medichelper.UserSessionManager;

public class ChangePasswordFragment extends Fragment {

    private EditText etCurrentPassword, etNewPassword, etConfirmNewPassword;
    private SwitchCompat switchFingerprint;
    private Button btnUpdatePassword;
    private ChangePasswordViewModel changePasswordViewModel;
    private SharedPreferences sharedPreferences;
    private UserSessionManager userSessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        // Initialize UI elements
        etCurrentPassword = view.findViewById(R.id.et_current_password);
        etNewPassword = view.findViewById(R.id.et_new_password);
        etConfirmNewPassword = view.findViewById(R.id.et_confirm_new_password);
        switchFingerprint = view.findViewById(R.id.switch_fingerprint);
        btnUpdatePassword = view.findViewById(R.id.btn_update_password);

        userSessionManager = UserSessionManager.getInstance();

        if (userSessionManager.isFingerprintEnabled()) {
            switchFingerprint.setChecked(true);
        }

        // Initialize ViewModel
        changePasswordViewModel = new ViewModelProvider(this).get(ChangePasswordViewModel.class);

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

        getContext();
        sharedPreferences = getActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        btnUpdatePassword.setOnClickListener(v -> handleChangePassword());

        changePasswordViewModel.getChangePasswordResponse().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                userSessionManager.setFingerprintEnabled(switchFingerprint.isChecked());
                // Password change successful
                Toast.makeText(getActivity(), "Password change successful!", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getActivity(), "Password change failed!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
    private void handleChangePassword() {
        String currentPassword = etCurrentPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();
        String confirmNewPassword = etConfirmNewPassword.getText().toString();
        boolean fingerprintEnabled = switchFingerprint.isChecked();

        String email = sharedPreferences.getString("user_email", null);
        String token = sharedPreferences.getString("jwt_token", null);

        if (email == null || token == null) {
            Toast.makeText(getContext(), "Unable to retrieve user info", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call ViewModel to change password
        changePasswordViewModel.changePassword(email, currentPassword, newPassword, confirmNewPassword, fingerprintEnabled, token);
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


}

