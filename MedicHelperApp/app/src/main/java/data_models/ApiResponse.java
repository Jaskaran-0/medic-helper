package data_models;

public class ApiResponse {
    private boolean successful;
    private String message;

    public ApiResponse(boolean successful, String message) {
        this.successful = successful;
        this.message = message;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "successful=" + successful +
                ", message='" + message + '\'' +
                '}';
    }
}
