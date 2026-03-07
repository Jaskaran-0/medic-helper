package ca.mohawk.medichelper;

import java.util.Date;

public class UserSessionManager {

    // FIX: Added 'volatile' so that the instance reference is always read from main memory,
    // not a thread-local CPU cache. Without this, a second thread could see a partially
    // constructed object. Combined with the double-checked lock below, this makes the
    // singleton safe for multi-threaded access without synchronizing every call to getInstance().
    private static volatile UserSessionManager instance;

    // User information
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Date dob;
    private boolean fingerprintEnabled;

    // Private constructor to prevent instantiation
    private UserSessionManager() {}

    // FIX: Added double-checked locking to make the singleton thread-safe.
    // The original code had a race condition: two threads could both see instance == null
    // simultaneously and each create a separate instance, breaking the singleton guarantee.
    public static UserSessionManager getInstance() {
        if (instance == null) {
            synchronized (UserSessionManager.class) {
                if (instance == null) {
                    instance = new UserSessionManager();
                }
            }
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
