package ca.mohawk.medichelper.ui.checklist;

import android.content.Context;
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

import java.util.List;

import ca.mohawk.medichelper.R;
import ca.mohawk.medichelper.activities.MainActivity;
import ca.mohawk.medichelper.api.ApiClient;
import ca.mohawk.medichelper.api.ApiService;
import data_models.Reminder;

public class ChecklistFragment extends Fragment {

    private ChecklistViewModel checklistViewModel;
    private RecyclerView recyclerView;
    private ReminderAdapter adapter;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checklist, container, false);

        progressBar = view.findViewById(R.id.progress_bar);
        recyclerView = view.findViewById(R.id.recycler_checklist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        view.findViewById(R.id.fab).setOnClickListener(v -> {
            Log.d("reminderFragment", "FAB clicked to add reminder");
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_add_reminder);
        });

        checklistViewModel = new ViewModelProvider(this).get(ChecklistViewModel.class);
        checklistViewModel.setApiService(apiService);

        adapter = new ReminderAdapter(checklistViewModel);
        recyclerView.setAdapter(adapter);

        observeViewModel();
        fetchReminders();

        return view;
    }

    private void observeViewModel() {
        checklistViewModel.getReminders().observe(getViewLifecycleOwner(), reminders -> adapter.setReminders(reminders));

        checklistViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void fetchReminders() {
        String jwtToken = getToken();
        if (jwtToken == null) {
            Toast.makeText(getContext(), "Please login again", Toast.LENGTH_SHORT).show();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).logout(null);
            }
            return;
        }
        checklistViewModel.fetchReminders(getToken());
    }

    private String getToken() {
        getContext();
        return getActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
                .getString("jwt_token", null);
    }
}
