package ca.mohawk.medichelper.ui.appointments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.CalendarContract;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import ca.mohawk.medichelper.R;
import ca.mohawk.medichelper.activities.MainActivity;

public class AddAppointmentFragment extends Fragment {

    private static final String TAG = "AddAppointmentFragment";

    private EditText etTitle, etDescription;
    private Button btnDatePicker, btnTimePicker, btnAddAppointment;
    private ProgressBar progressBar;
    private AddAppointmentViewModel addAppointmentViewModel;

    private String selectedDate, selectedTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_appointment, container, false);

        etTitle = view.findViewById(R.id.et_title);
        etDescription = view.findViewById(R.id.et_description);
        btnDatePicker = view.findViewById(R.id.btn_date_picker);
        btnTimePicker = view.findViewById(R.id.btn_time_picker);
        btnAddAppointment = view.findViewById(R.id.btn_add_appointment);
        progressBar = view.findViewById(R.id.progress_bar);

        addAppointmentViewModel = new ViewModelProvider(this).get(AddAppointmentViewModel.class);

        btnDatePicker.setOnClickListener(v -> {
            Log.d(TAG, "Date picker clicked");
            openDatePicker();
        });
        btnTimePicker.setOnClickListener(v -> {
            Log.d(TAG, "Time picker clicked");
            openTimePicker();
        });
        btnAddAppointment.setOnClickListener(v -> {
            Log.d(TAG, "Add appointment button clicked");
            addAppointment();
        });

        setupObservers();
        return view;
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            // Create a Calendar object for the selected date
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(year, month, dayOfMonth); // Set the selected date

            // FIX: Zero out time-of-day before converting to UTC. Without this, the Calendar
            // retains the current hour/minute/second, so users in UTC- timezones who pick a
            // date in the evening would see it sent as the previous calendar day in UTC
            // (e.g., picking Jan 15 at 8 PM EST → "2024-01-16T01:00:00Z", or more critically,
            // picking at 6 PM EST → "2024-01-15T23:00:00Z" which rounds to Jan 15, but
            // picking at 11 PM EST → "2024-01-16T04:00:00Z" which stores as Jan 16).
            selectedCalendar.set(Calendar.HOUR_OF_DAY, 0);
            selectedCalendar.set(Calendar.MINUTE, 0);
            selectedCalendar.set(Calendar.SECOND, 0);
            selectedCalendar.set(Calendar.MILLISECOND, 0);

            // Format the selected date as "yyyy-MM-dd" to display in the UI
            SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            displayFormat.setTimeZone(TimeZone.getDefault());
            String displayedDate = displayFormat.format(selectedCalendar.getTime());
            btnDatePicker.setText(displayedDate);

            // Format the date in UTC format for sending to backend
            SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            selectedDate = utcFormat.format(selectedCalendar.getTime());

            Log.d(TAG, "Selected date (local): " + displayedDate);
            Log.d(TAG, "Selected date (UTC): " + selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }


    private void openTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            selectedTime = String.format("%02d:%02d:00", hourOfDay, minute);
            btnTimePicker.setText(selectedTime);
            Log.d(TAG, "Selected time: " + selectedTime);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    private void addAppointment() {
        String title = etTitle.getText().toString();
        String description = etDescription.getText().toString();

        if (title.isEmpty() || description.isEmpty() || selectedDate == null || selectedTime == null) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Failed to add appointment: missing fields");
            return;
        }

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("jwt_token", null);

        if (token != null) {
            Log.d(TAG, "Adding appointment with token: " + token);
            progressBar.setVisibility(View.VISIBLE);
            addAppointmentViewModel.addAppointment(token, title, description, selectedDate, selectedTime);

//            TODO: adding appointments to calendar on google pixel not possible right now as google auth is not being used
//            Log.d(TAG, "Opening calendar to add event");
//            addAppointmentToCalendar(title, description, selectedDate, selectedTime);
        } else {
            Toast.makeText(getContext(), "Please log in", Toast.LENGTH_SHORT).show();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).logout(null);
            }
            Log.e(TAG, "No token found, user not logged in");
        }

    }

    private void addAppointmentToCalendar(String title, String description, String date, String time) {
        try {
            // Ensure `date` is in "yyyy-MM-dd" format and `time` in "HH:mm:ss" format
            if (date.contains("T")) {
                date = date.split("T")[0]; // Extract only the date part
            }
            if (time.contains("Z")) {
                time = time.replace("Z", ""); // Remove 'Z' if it exists
            }

            String[] dateParts = date.split("-");
            String[] timeParts = time.split(":");

            // Parse and set date and time in Calendar object
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, Integer.parseInt(dateParts[0]));
            calendar.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1); // Month is 0-based
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateParts[2]));
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
            calendar.set(Calendar.SECOND, 0);

            long startMillis = calendar.getTimeInMillis();
            long endMillis = startMillis + 60 * 60 * 1000; // 1-hour duration

            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setData(CalendarContract.Events.CONTENT_URI);
            intent.putExtra(CalendarContract.Events.TITLE, title);
            intent.putExtra(CalendarContract.Events.DESCRIPTION, description);
            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis);
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis);
            intent.putExtra(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(intent);
                Log.d(TAG, "Google Calendar event created successfully");
            } else {
                Toast.makeText(getContext(), "No calendar app found", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "No calendar app available on device");
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            Toast.makeText(getContext(), "Failed to parse date/time format", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error parsing date/time: " + e.getMessage());
        }
    }

    private void setupObservers() {
        addAppointmentViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                btnAddAppointment.setEnabled(!isLoading);
                Log.d(TAG, "Loading state: " + isLoading);
            }
        });

        addAppointmentViewModel.isAppointmentAdded().observe(getViewLifecycleOwner(), isAdded -> {
            if (isAdded != null && isAdded) {
                Toast.makeText(getContext(), "Appointment added successfully", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Appointment added successfully");
                requireActivity().onBackPressed();
            } else if (isAdded != null) {
                Toast.makeText(getContext(), "Failed to add appointment", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to add appointment");
            }
        });
    }
}
