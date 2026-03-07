package ca.mohawk.medichelper.ui.appointments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import data_models.Appointment;
import ca.mohawk.medichelper.activities.MainActivity;

public class AppointmentFragment extends Fragment {

    private AppointmentViewModel appointmentViewModel;
    private RecyclerView recyclerView;
    private AppointmentAdapter appointmentAdapter;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointments, container, false);

        recyclerView = view.findViewById(R.id.recycler_appointments);
        progressBar = view.findViewById(R.id.progress_bar);

        appointmentViewModel = new ViewModelProvider(this).get(AppointmentViewModel.class);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("jwt_token", null);
        if (token == null) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).logout(null);
            }
            return view;
        }

        appointmentAdapter = new AppointmentAdapter(new AppointmentAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(Appointment appointment) {
                progressBar.setVisibility(View.VISIBLE);
                appointmentViewModel.deleteAppointment(token, appointment.getAppointmentId());
            }
        });
        recyclerView.setAdapter(appointmentAdapter);

        appointmentViewModel.getAppointments().observe(getViewLifecycleOwner(), appointments -> {
            appointmentAdapter.setAppointments(appointments);
            progressBar.setVisibility(View.GONE);
        });

        appointmentViewModel.getDeleteStatus().observe(getViewLifecycleOwner(), isDeleted -> {
            progressBar.setVisibility(View.GONE);
            if (isDeleted != null && isDeleted) {
                Toast.makeText(getContext(), "Appointment deleted", Toast.LENGTH_SHORT).show();
                appointmentViewModel.resetDeleteStatus(); // Reset deleteStatus to prevent multiple toasts
                appointmentViewModel.fetchAppointments(token); // Refresh appointments
            } else if (isDeleted != null) {
                Toast.makeText(getContext(), "Failed to delete appointment", Toast.LENGTH_SHORT).show();
                appointmentViewModel.resetDeleteStatus(); // Reset deleteStatus
            }
        });

        appointmentViewModel.fetchAppointments(token);

        // Handle FAB click
        view.findViewById(R.id.fab_add_appointment).setOnClickListener(v -> {
            Log.d("AppointmentFragment", "FAB clicked to add appointment");
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_add_appointment);
        });

        return view;
    }
}
