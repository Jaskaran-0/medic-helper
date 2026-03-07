package data_models;

public class ChangePasswordDTO {

    private String email;
    private String currentPassword;
    private String newPassword;
    private boolean fingerprintEnabled;

    public ChangePasswordDTO(String email, String currentPassword, String newPassword, boolean fingerprintEnabled) {
        this.email = email;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.fingerprintEnabled = fingerprintEnabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public boolean isFingerprintEnabled() {
        return fingerprintEnabled;
    }

    public void setFingerprintEnabled(boolean fingerprintEnabled) {
        this.fingerprintEnabled = fingerprintEnabled;
    }

    @Override
    public String toString() {
        return "ChangePasswordDTO{" +
                "email='" + email + '\'' +
                ", currentPassword='" + currentPassword + '\'' +
                ", newPassword='" + newPassword + '\'' +
                ", fingerprintEnabled=" + fingerprintEnabled +
                '}';
    }
}
