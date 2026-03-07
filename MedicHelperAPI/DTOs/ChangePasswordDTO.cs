using System.ComponentModel.DataAnnotations;

namespace MedicHelperAPI.DTOs;

public class ChangePasswordDTO
{
    [Required]
    [EmailAddress]
    public string Email { get; set; }

    [Required]
    public string CurrentPassword { get; set; }

    [Required]
    [DataType(DataType.Password)]
    public string NewPassword { get; set; }

    public bool? FingerprintEnabled { get; set; }
}
