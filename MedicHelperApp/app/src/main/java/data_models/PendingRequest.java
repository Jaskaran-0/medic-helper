package data_models;

public class PendingRequest {
    private int familyMemberId; // Unique identifier for the request
    private RequestingUser requestingUser; // Nested object for the user who sent the request
    private String requestedOn; // The date when the request was sent

    public PendingRequest(int familyMemberId, RequestingUser requestingUser, String requestedOn) {
        this.familyMemberId = familyMemberId;
        this.requestingUser = requestingUser;
        this.requestedOn = requestedOn;
    }

    public int getFamilyMemberId() {
        return familyMemberId;
    }

    public void setFamilyMemberId(int familyMemberId) {
        this.familyMemberId = familyMemberId;
    }

    public RequestingUser getRequestingUser() {
        return requestingUser;
    }

    public void setRequestingUser(RequestingUser requestingUser) {
        this.requestingUser = requestingUser;
    }

    public String getRequestedOn() {
        return requestedOn;
    }

    public void setRequestedOn(String requestedOn) {
        this.requestedOn = requestedOn;
    }

    // Nested class for the requesting user details
    public static class RequestingUser {
        private String firstName;
        private String lastName;
        private String email;

        public RequestingUser(String firstName, String lastName, String email) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
        }

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
    }
}
