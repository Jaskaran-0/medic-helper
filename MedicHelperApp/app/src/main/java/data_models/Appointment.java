package data_models;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class Appointment {
    private int appointmentId;
    private final String date;
    private final String time;
    private final String title;
    private final String description;
    private boolean isDeleted;


    public Appointment(String title, String description, String date, String time) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
    }

    public Appointment(int appointmentId, String date, String time, String title, String description, boolean isDeleted) {
        this.appointmentId = appointmentId;
        this.date = date;
        this.time = time;
        this.title = title;
        this.description = description;
        this.isDeleted = isDeleted;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "appointmentId=" + appointmentId +
                ", date=" + date +
                ", time=" + time +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
