package data_models;
public class AuthResponse {

    public AuthResponse(String token) {
        this.token = token;
    }

    private String token;  // Assuming your login response contains a token.

    // Getters and setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "token='" + token + '\'' +
                '}';
    }
}