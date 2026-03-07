package data_models;

import java.io.Serializable;
import java.util.Date;

public class UserDTO implements Serializable {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Date dob; // Date of birth
    private boolean fingerprintEnabled;

    // Constructor
    public UserDTO(String firstName, String lastName, String email, String phoneNumber, boolean fingerprintEnabled, Date dob) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.fingerprintEnabled = fingerprintEnabled;
        this.dob = dob;
    }

    // Getters and Setters
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

    public boolean isFingerprintEnabled() {
        return fingerprintEnabled;
    }

    public void setFingerprintEnabled(boolean fingerprintEnabled) {
        this.fingerprintEnabled = fingerprintEnabled;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", dob=" + dob +
                ", fingerprintEnabled=" + fingerprintEnabled +
                '}';
    }
}
