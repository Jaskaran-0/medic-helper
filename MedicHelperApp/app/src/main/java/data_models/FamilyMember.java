package data_models;

public class FamilyMember {
    private String familyMemberId; // ID of the family member
    private final String familyUserId;
    private final String firstName; // First name of the family member
    private final String lastName; // Last name of the family member
    private String email; // Email of the family member
    private final String approvedOn; // The date when the member was added

    public FamilyMember(String familyMemberId, String familyUserId, String firstName, String lastName, String email, String addedOn) {
        this.familyMemberId = familyMemberId;
        this.familyUserId = familyUserId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.approvedOn = addedOn;
    }

    public String getFamilyMemberId() {
        return familyMemberId;
    }

    public void setFamilyMemberId(String familyMemberId) {
        this.familyMemberId = familyMemberId;
    }

    public String getFullName() {
        return firstName+" "+lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddedOn() {
        return approvedOn;
    }

    public String getFamilyUserId() {
        return familyUserId;
    }
}
