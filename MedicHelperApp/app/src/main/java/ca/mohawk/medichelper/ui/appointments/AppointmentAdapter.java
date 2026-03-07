package ca.mohawk.medichelper.ui.appointments;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import ca.mohawk.medichelper.R;
import data_models.Appointment;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointments;
    private final OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Appointment appointment);
    }

    public AppointmentAdapter(OnDeleteClickListener deleteClickListener) {
        this.deleteClickListener = deleteClickListener;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);

        holder.tvTitle.setText(appointment.getTitle());
        holder.tvDescription.setText(appointment.getDescription());

        try {
            // Parse the date string from the backend (UTC) to a ZonedDateTime
            DateTimeFormatter inputDateFormatter = DateTimeFormatter.ISO_DATE_TIME; // Matches format like "2024-11-15T21:32:20.801355Z"
            ZonedDateTime utcDateTime = ZonedDateTime.parse(appointment.getDate(), inputDateFormatter);

            // Convert the UTC time to the local time zone
            ZonedDateTime localDateTime = utcDateTime.withZoneSameInstant(ZoneId.systemDefault());

            // Format the local date to "dd MMM yyyy"
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            String formattedDate = localDateTime.format(dateFormatter);

            // Parse the time string from appointment.getTime()
            LocalTime time = LocalTime.parse(appointment.getTime(), DateTimeFormatter.ofPattern("HH:mm:ss"));

            // Format the time to "hh:mm a"
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
            String formattedTime = time.format(timeFormatter);

            // Set the formatted values to the TextViews
            holder.tvDate.setText(formattedDate);
            holder.tvTime.setText(formattedTime);

            Log.d("AppointmentAdapter", "Date (UTC): " + utcDateTime + ", Date (Local): " + localDateTime);
        } catch (Exception e) {
            // Handle parsing errors gracefully
            holder.tvDate.setText("Invalid date");
            holder.tvTime.setText("Invalid time");
            Log.e("AppointmentAdapter", "Error parsing date/time", e);
        }

        holder.btnDelete.setOnClickListener(v -> deleteClickListener.onDeleteClick(appointment));
    }


    @Override
    public int getItemCount() {
        return appointments != null ? appointments.size() : 0;
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvDate, tvTime;
        Button btnDelete;

        AppointmentViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTime = itemView.findViewById(R.id.tv_time);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
