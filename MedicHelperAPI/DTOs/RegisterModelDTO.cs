using System.ComponentModel.DataAnnotations;

namespace MedicHelperAPI.DTOs
{
    public class RegisterModelDTO
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

        [Required]
        [DataType(DataType.Date)]
        public DateTime Dob { get; set; }

        [Required]
        [MinLength(8, ErrorMessage = "Password must be at least 8 characters long.")]
        public string PasswordHash { get; set; }

        [Required]
        public string FCMToken { get; set; }

        public bool FingerprintEnabled { get; set; } = false; // default is false if not provided
    }
}
