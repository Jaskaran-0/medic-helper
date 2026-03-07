package data_models;

public class AddFamilyMemberDTO {
    private String email;

    public AddFamilyMemberDTO(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
