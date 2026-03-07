package data_models;

public class SwitchAccountResponse {

    private final String token;
    private final String token1;

    public SwitchAccountResponse(String token, String token1) {
        this.token = token;
        this.token1 = token1;
    }

    public String getToken() {
        return token;
    }

    public String getToken1() {
        return token1;
    }

    @Override
    public String toString() {
        return "SwitchAccountResponse{" +
                "token='" + token + '\'' +
                ", token1='" + token1 + '\'' +
                '}';
    }
}
