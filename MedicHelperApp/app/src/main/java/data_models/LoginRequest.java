package data_models;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginRequest {
    private String email;
    private String passwordHash;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.passwordHash = password;
    }

    // Getters and setters (optional if you're using Gson)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "email='" + email + '\'' +
                ", password='" + passwordHash + '\'';
    }

}
