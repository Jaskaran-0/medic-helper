package ca.mohawk.medichelper.ui.family;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ca.mohawk.medichelper.R;
import ca.mohawk.medichelper.activities.MainActivity;

public class AddFamilyFragment extends Fragment {

    private AddFamilyViewModel addFamilyViewModel;
    private EditText etFamilyEmail;
    private Button btnSendRequest;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_family, container, false);

        etFamilyEmail = view.findViewById(R.id.et_family_email);
        btnSendRequest = view.findViewById(R.id.btn_send_request);

        addFamilyViewModel = new ViewModelProvider(this).get(AddFamilyViewModel.class);

        // Get JWT token from SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString("jwt_token", null);

        if (jwtToken == null) {
            Log.d("FamilyFragment", "JWT Token is null");
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).logout(null);
            }
            Toast.makeText(getContext(), "Please Login again", Toast.LENGTH_SHORT).show();
        }

        btnSendRequest.setOnClickListener(v -> {
            String email = etFamilyEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(getContext(), "Please enter an email", Toast.LENGTH_SHORT).show();
                return;
            }

            addFamilyViewModel.sendFamilyRequest(jwtToken, email);
        });

        // Observe response
        addFamilyViewModel.getAddFamilyResponse().observe(getViewLifecycleOwner(), response -> {
            if (response != null) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Request sent successfully", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed(); // Navigate back
                } else {
                    Toast.makeText(getContext(), "Failed to send request: " + response.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}
