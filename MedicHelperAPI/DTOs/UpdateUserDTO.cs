using System.ComponentModel.DataAnnotations;

namespace MedicHelperAPI.DTOs;

public class UpdateUserDTO
{
    [Required]
    public string FirstName { get; set; }

    [Required]
    public string LastName { get; set; }

    [Required]
    [EmailAddress]
    public string Email { get; set; }

    [Required]
    [Phone]
    [StringLength(12, MinimumLength = 10, ErrorMessage = "Phone number must be between 10 and 12 digits.")]
    public string PhoneNumber { get; set; }
}
