namespace MedicHelperAPI.DTOs
{
    public class FamilyMemberDTO
    {
        public int FamilyMemberId { get; set; }
        public string FamilyUserId { get; set; }
        public string FirstName { get; set; }
        public string LastName { get; set; }
        public string Email { get; set; }
        public DateTime ApprovedOn { get; set; }
    }
}
