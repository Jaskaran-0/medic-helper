package ca.mohawk.medichelper;

import java.util.Date;

public class UserSessionManager {

    private static UserSessionManager instance;

    // User information
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Date dob;
    private boolean fingerprintEnabled;

    // Private constructor to prevent instantiation
    private UserSessionManager() {}

    // Singleton pattern: getInstance() to get the single instance of this class
    public static UserSessionManager getInstance() {
        if (instance == null) {
            instance = new UserSessionManager();
        }
        return instance;
    }

    // Set user information
    public void setUserDetails(String firstName, String lastName, String email, String phoneNumber, Date dob, boolean fingerprintEnabled) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dob = dob;
        this.fingerprintEnabled = fingerprintEnabled;
    }

    // Get user information
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Date getDob() {
        return dob;
    }

    public boolean isFingerprintEnabled() {
        return fingerprintEnabled;
    }

    public void setFingerprintEnabled(boolean enabled) {
        this.fingerprintEnabled = enabled;
    }

    public void clearUserDetails() {
        this.firstName = null;
        this.lastName = null;
        this.email = null;
        this.phoneNumber = null;
        this.dob = null;
        this.fingerprintEnabled = false;
    }

    @Override
    public String toString() {
        return "UserSessionManager{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", dob=" + dob +
                ", fingerprintEnabled=" + fingerprintEnabled +
                '}';
    }
}
