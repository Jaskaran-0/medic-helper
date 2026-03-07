using System.ComponentModel.DataAnnotations;

namespace MedicHelperAPI.DTOs;

public class UpdateReminderDTO
{
    [Required]
    public int ReminderId { get; set; }

    [Required]
    public bool MedicationTaken { get; set; }

    [Required]
    public int Dosage { get; set; }
}
