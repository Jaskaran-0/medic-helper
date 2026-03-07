package data_models;

public class Reminder {
    private int medicationId;
    private final int dosage;
    private String repeatDays;
    private String time;
    private int reminderId;
    private boolean medicationTaken;
    private String medicationName;

    public Reminder(int medicationId, int dosage, String repeatDays, String time) {
        this.medicationId= medicationId;
        this.dosage = dosage;
        this.repeatDays = repeatDays;
        this.time = time;
    }

    public Reminder( int reminderId, String time, String repeatDays, int dosage, int medicationId, String medicationName, boolean medicationTaken) {
        this.reminderId = reminderId;
        this.time = time;
        this.repeatDays = repeatDays;
        this.dosage = dosage;
        this.medicationId = medicationId;
        this.medicationName = medicationName;
        this.medicationTaken = medicationTaken;

    }

    public Reminder(int reminderId, boolean medicationTaken, int dosage) {
        this.reminderId= reminderId;
        this.medicationTaken= medicationTaken;
        this.dosage= dosage;
    }

    public int getMedicationId() {
        return medicationId;
    }

    public int getDosage() {
        return dosage;
    }

    public String getRepeatDays() {
        return repeatDays;
    }

    public String getTime() {
        return time;
    }

    public int getReminderId() {
        return reminderId;
    }

    public boolean isMedicationTaken() {
        return medicationTaken;
    }

    public String getMedicationName() {
        return medicationName;
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "medicationId=" + medicationId +
                ", dosage='" + dosage + '\'' +
                ", repeatDays='" + repeatDays + '\'' +
                ", time='" + time + '\'' +
                ", reminderId=" + reminderId +
                ", medicationTaken=" + medicationTaken +
                '}';
    }
}
