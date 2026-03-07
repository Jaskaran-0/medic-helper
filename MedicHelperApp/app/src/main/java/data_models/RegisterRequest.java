package data_models;

import java.util.Date;

public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String dob; // Date of birth
    private String passwordHash;
    private boolean fingerprintEnabled;
    private final String fcmToken;

    // Constructor, Getters, and Setters

    public RegisterRequest(String firstName, String lastName, String email, String phoneNumber, String dob, String passwordHash, boolean fingerprintEnabled, String fcmToken) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dob = dob;
        this.passwordHash = passwordHash;
        this.fingerprintEnabled = fingerprintEnabled;
        this.fcmToken=fcmToken;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getPassword() {
        return passwordHash;
    }

    public void setPassword(String password) {
        this.passwordHash = password;
    }


    public boolean isFingerprintEnabled() {
        return fingerprintEnabled;
    }

    public void setFingerprintEnabled(boolean fingerprintEnabled) {
        this.fingerprintEnabled = fingerprintEnabled;
    }

    @Override
    public String toString() {
        return "RegisterRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", dob='" + dob + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", fingerprintEnabled=" + fingerprintEnabled +
                ", fcmToken='" + fcmToken + '\'' +
                '}';
    }
}
