package ca.mohawk.medichelper.ui.checklist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ca.mohawk.medichelper.R;
import data_models.Reminder;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private List<Reminder> reminders;
    private final ChecklistViewModel checklistViewModel;

    public ReminderAdapter(ChecklistViewModel checklistViewModel) {
        this.checklistViewModel = checklistViewModel;
    }

    public void setReminders(List<Reminder> reminders) {
        this.reminders = reminders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);
        holder.bind(reminder);
    }

    @Override
    public int getItemCount() {
        return reminders != null ? reminders.size() : 0;
    }

    class ReminderViewHolder extends RecyclerView.ViewHolder {

        private final TextView medicationNameTextView;
        private final TextView timeTextView;
        private final TextView dosageTextView;
        private final CheckBox medicationTakenCheckBox;
        private final Button deleteButton;

        ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            medicationNameTextView = itemView.findViewById(R.id.tv_medication_name);
            timeTextView = itemView.findViewById(R.id.tv_reminder_time);
            dosageTextView = itemView.findViewById(R.id.tv_reminder_dosage);
            medicationTakenCheckBox = itemView.findViewById(R.id.cb_medication_taken);
            deleteButton = itemView.findViewById(R.id.btn_delete_reminder);
        }

        void bind(Reminder reminder) {

            Log.d("ReminderAdapter", "Binding reminder: " + reminder);

            medicationNameTextView.setText(reminder.getMedicationName());  // Set medication name
            timeTextView.setText(reminder.getTime());
            int dosage = reminder.getDosage();
            dosageTextView.setText(Integer.toString(dosage));
            medicationTakenCheckBox.setChecked(reminder.isMedicationTaken());

            if(reminder.isMedicationTaken()){
                medicationTakenCheckBox.setEnabled(false);
            }

            medicationTakenCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                checklistViewModel.updateMedicationTaken(reminder.getReminderId(), isChecked, getToken(), reminder.getDosage());
                if(isChecked){
                    medicationTakenCheckBox.setEnabled(false);
                }
            });

            deleteButton.setOnClickListener(v -> checklistViewModel.deleteReminder(reminder.getReminderId(), getToken()));
        }

        private String getToken() {
            itemView.getContext();
            return itemView.getContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
                    .getString("jwt_token", null);
        }
    }
}
