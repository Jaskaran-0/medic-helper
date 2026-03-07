using System;

namespace MedicHelperAPI.Models
{
    public class Family
    {
        public int FamilyMemberId { get; set; } // Primary Key

        public string UserId { get; set; } // The user who is sending the request
        public string FamilyUserId { get; set; } // The family member being requested

        public DateTime RequestedOn { get; set; } // When the request was sent
        public DateTime? ApprovedOn { get; set; } // When the request was approved (if applicable)
        public DateTime? UpdatedOn { get; set; } // When the relationship was last updated
        public bool IsApproved { get; set; } // Indicates if the request has been approved
        public bool IsRemoved { get; set; } // Flag to indicate removal of the relationship

        // Navigation properties
        public User User { get; set; } // Navigation for the user who sent the request
        public User FamilyUser { get; set; } // Navigation for the requested family member
    }
}
