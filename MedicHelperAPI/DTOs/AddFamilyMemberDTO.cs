using System.ComponentModel.DataAnnotations;

namespace MedicHelperAPI.DTOs;

public class AddFamilyMemberDTO
{
    [Required]
    [EmailAddress]
    public string Email { get; set; }
}
