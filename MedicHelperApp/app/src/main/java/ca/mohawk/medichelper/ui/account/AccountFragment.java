package ca.mohawk.medichelper.ui.account;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import ca.mohawk.medichelper.R;
import ca.mohawk.medichelper.activities.MainActivity;

public class AccountFragment extends Fragment {

    private static final String TAG = "AccountFragment";

    private EditText etFirstName, etLastName, etEmail, etPhoneNumber, etDob;
    private Button btnSave;
    private AccountViewModel accountViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Initialize UI elements
        etFirstName = view.findViewById(R.id.et_first_name);
        etLastName = view.findViewById(R.id.et_last_name);
        etEmail = view.findViewById(R.id.et_email);
        etPhoneNumber = view.findViewById(R.id.et_phone_number);
        etDob = view.findViewById(R.id.et_dob); // Date of birth, disabled for editing
        btnSave = view.findViewById(R.id.btn_save);

        // Initialize ViewModel
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        // Load user details from ViewModel (session manager)
        loadUserDetails();

        // Save button listener
        btnSave.setOnClickListener(v -> saveUserDetails());

        // Observe ViewModel LiveData to check for successful update
        accountViewModel.updateSuccess.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean success) {
                if (success) {
                    Toast.makeText(getContext(), "User details updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to update user details", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    // Load the user details from the session
    private void loadUserDetails() {
        etFirstName.setText(accountViewModel.getUserSessionManager().getFirstName());
        etLastName.setText(accountViewModel.getUserSessionManager().getLastName());
        etEmail.setText(accountViewModel.getUserSessionManager().getEmail());
        etPhoneNumber.setText(accountViewModel.getUserSessionManager().getPhoneNumber());
        etDob.setText(accountViewModel.getUserSessionManager().getDob().toString()); // Date of birth (disabled)
    }

    // Save the user details using ViewModel
    private void saveUserDetails() {
        String firstName = etFirstName.getText().toString();
        String lastName = etLastName.getText().toString();
        String email = etEmail.getText().toString();
        String phoneNumber = etPhoneNumber.getText().toString();

        // Get the JWT token from SharedPreferences
        getContext();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("jwt_token", null);

        if (token != null) {
            accountViewModel.saveUserDetails(token, firstName, lastName, email, phoneNumber);
        }
        else {
            Toast.makeText(getContext(), "Please login again", Toast.LENGTH_SHORT).show();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).logout(null);
            }
        }
    }
}
