using System.ComponentModel.DataAnnotations;

namespace MedicHelperAPI.DTOs
{
    public class LoginModelDTO
    {
        [Required]
        public string Email { get; set; }

        public string PasswordHash { get; set; }
        public bool FingerprintLoginAuth {  get; set; }=false;
        
        [Required]
        public bool FingerprintEnabled { get; set; }
    }
}
