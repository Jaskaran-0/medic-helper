package ca.mohawk.medichelper.ui.checklist;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ca.mohawk.medichelper.R;
import ca.mohawk.medichelper.activities.MainActivity;
import data_models.Medication;
import data_models.Reminder;

public class AddReminderFragment extends Fragment {

    private AddReminderViewModel viewModel;
    private Spinner spinnerMedications;
    private EditText etDosage;
    private Button btnTimePicker;
    private Button btnSaveReminder;
    private List<CheckBox> dayCheckboxes;
    private List<Medication> medications;

    private int reminderHour = -1;
    private int reminderMinute = -1;
    private int selectedMedicationId = -1;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_reminder, container, false);

        viewModel = new ViewModelProvider(this).get(AddReminderViewModel.class);
        spinnerMedications = view.findViewById(R.id.spinner_medications);
        etDosage = view.findViewById(R.id.et_dosage);
        btnTimePicker = view.findViewById(R.id.btn_time_picker);
        btnSaveReminder = view.findViewById(R.id.btn_save_reminder);

        dayCheckboxes = new ArrayList<>();
        dayCheckboxes.add(view.findViewById(R.id.cb_mon));
        dayCheckboxes.add(view.findViewById(R.id.cb_tue));
        dayCheckboxes.add(view.findViewById(R.id.cb_wed));
        dayCheckboxes.add(view.findViewById(R.id.cb_thu));
        dayCheckboxes.add(view.findViewById(R.id.cb_fri));
        dayCheckboxes.add(view.findViewById(R.id.cb_sat));
        dayCheckboxes.add(view.findViewById(R.id.cb_sun));

        // Observe medications data
        viewModel.getMedicationsLiveData().observe(getViewLifecycleOwner(), medications -> {
            if (medications != null) {
                setupMedicationSpinner(medications);
                this.medications= medications;
            } else {
                Toast.makeText(getContext(), "Failed to load medications", Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch medications
        getContext();
        String token = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
                .getString("jwt_token", null);
        if (token != null) {
            viewModel.fetchMedications(token);
        }
        else{
            Toast.makeText(getContext(), "Token not found. Please log in.", Toast.LENGTH_SHORT).show();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).logout(null);
            }
        }

        // Set time picker for reminder time
        btnTimePicker.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (TimePicker timePicker, int selectedHour, int selectedMinute) -> {
                reminderHour = selectedHour;
                reminderMinute = selectedMinute;
                btnTimePicker.setText(String.format("%02d:%02d:%02d", reminderHour, reminderMinute, 0));
            }, hour, minute, true);

            timePickerDialog.show();
        });

        // Save reminder button click
        btnSaveReminder.setOnClickListener(v -> saveReminder());

        // Observe if the reminder was added
        viewModel.isReminderAdded().observe(getViewLifecycleOwner(), isAdded -> {
            if (isAdded) {
                Toast.makeText(getContext(), "Reminder added successfully", Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed(); // Navigate back
            } else {
                Toast.makeText(getContext(), "Failed to add reminder", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void setupMedicationSpinner(List<Medication> medications) {
        List<String> medicationNames = new ArrayList<>();
        for (Medication med : medications) {
            medicationNames.add(med.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, medicationNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMedications.setAdapter(adapter);

        spinnerMedications.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMedicationId = medications.get(position).getMedicationId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedMedicationId = -1;
            }
        });
    }

    private void saveReminder() {
        if (reminderHour == -1 || reminderMinute == -1) {
            Toast.makeText(getContext(), "Please select a time for the reminder", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedMedicationId == -1) {
            Toast.makeText(getContext(), "Please select a medication", Toast.LENGTH_SHORT).show();
            return;
        }

        Medication selectedMedication= null;

        for (Medication m :
                medications) {
            if(m.getMedicationId() ==selectedMedicationId){
                selectedMedication = m;
            }
        }

        int dosage = Integer.parseInt(etDosage.getText().toString());

        if(selectedMedication.getInventory() < dosage){
            Toast.makeText(getContext(), "Dosage exceeds inventory", Toast.LENGTH_SHORT).show();
            return;
        }
        String selectedDays = getSelectedDays();

        if (selectedDays.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one repeat day", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedMedicationId == -1) {
            Toast.makeText(getContext(), "Please select a medication", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dosage == 0) {
            Toast.makeText(getContext(), "Please enter a dosage", Toast.LENGTH_SHORT).show();
            return;
        }

        Reminder reminder = new Reminder(
                selectedMedicationId,
                dosage,
                selectedDays,
                String.format("%02d:%02d:%02d", reminderHour, reminderMinute, 0)
        );

        getContext();
        String token = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
                .getString("jwt_token", null);

        if (token != null) {
            viewModel.addReminder(reminder, token);
        } else {
            Toast.makeText(getContext(), "Token not found. Please log in.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getSelectedDays() {
        StringBuilder selectedDays = new StringBuilder();
        for (CheckBox checkBox : dayCheckboxes) {
            if (checkBox.isChecked()) {
                selectedDays.append(checkBox.getText().toString()).append(",");
            }
        }
        return selectedDays.length() > 0 ? selectedDays.substring(0, selectedDays.length() - 1) : "";
    }
}
