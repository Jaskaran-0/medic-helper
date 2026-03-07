package ca.mohawk.medichelper.ui.family;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ca.mohawk.medichelper.R;
import ca.mohawk.medichelper.activities.MainActivity;
import data_models.FamilyMember;
import data_models.PendingRequest;

public class FamilyFragment extends Fragment {

    private FamilyViewModel familyViewModel;
    private RecyclerView rvFamilyMembers, rvPendingRequests;
    private Button btnAddFamily;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_family, container, false);

        rvFamilyMembers = view.findViewById(R.id.rv_family_members);
        rvPendingRequests = view.findViewById(R.id.rv_pending_requests);
        btnAddFamily = view.findViewById(R.id.btn_add_family);

        familyViewModel = new ViewModelProvider(this).get(FamilyViewModel.class);

        rvFamilyMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPendingRequests.setLayoutManager(new LinearLayoutManager(getContext()));

        //get JWT token from shared preferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString("jwt_token", null);

        if (jwtToken != null) {
            familyViewModel.setJwtToken(jwtToken);
        } else {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).logout(null);
            }
            Toast.makeText(getContext(), "Please Login again", Toast.LENGTH_SHORT).show();
        }


        // Set up Family Member Adapter
        FamilyMemberAdapter familyMemberAdapter = new FamilyMemberAdapter(null, new FamilyMemberAdapter.OnFamilyMemberActionListener() {
            @Override
            public void onGoToAccount(FamilyMember familyMember) {
                Toast.makeText(getContext(), "Switching to " + familyMember.getFullName(), Toast.LENGTH_SHORT).show();
                familyViewModel.switchToFamilyAccount(familyMember.getFamilyUserId());
            }

            @Override
            public void onRemove(FamilyMember familyMember) {
                Toast.makeText(getContext(), "Removing " + familyMember.getFullName(), Toast.LENGTH_SHORT).show();
                familyViewModel.removeFamilyMember(familyMember.getFamilyMemberId());
            }
        });

        rvFamilyMembers.setAdapter(familyMemberAdapter);

        // Set up Pending Request Adapter
        PendingRequestAdapter pendingRequestAdapter = new PendingRequestAdapter(null, new PendingRequestAdapter.OnPendingRequestActionListener() {
            @Override
            public void onApprove(PendingRequest request) {
                familyViewModel.approvePendingRequest(request);
                Toast.makeText(getContext(), "Approved: " + request.getRequestingUser().getEmail(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReject(PendingRequest request) {
                familyViewModel.rejectPendingRequest(request);
                Toast.makeText(getContext(), "Rejected: " + request.getRequestingUser().getEmail(), Toast.LENGTH_SHORT).show();
            }
        });

        rvPendingRequests.setAdapter(pendingRequestAdapter);

        // Observe Family Members
        familyViewModel.getFamilyMembers().observe(getViewLifecycleOwner(), familyMembers -> {
            if (familyMembers != null) {
                familyMemberAdapter.setFamilyMembers(familyMembers);
            }
        });

        // Observe Pending Requests
        familyViewModel.getPendingRequests().observe(getViewLifecycleOwner(), pendingRequests -> {
            if (pendingRequests != null) {
                pendingRequestAdapter.updatePendingRequests(pendingRequests);
            }
        });

        // Fetch data
        familyViewModel.fetchFamilyData();

        btnAddFamily.setOnClickListener(v -> {
            Log.d("FamilyFragment", "Add Family button clicked");

            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.add_family);
        });

        familyViewModel.getNewJwtToken().observe(getViewLifecycleOwner(), resp -> {
            if (resp != null) {

                Log.d("FamilyFragment", "New JWT Token received " );
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) requireActivity()).logout(resp);
                }

            }
        });

        return view;
    }
}
