using System.ComponentModel.DataAnnotations;

namespace MedicHelperAPI.DTOs;

public class ReminderDTO
{
    public int ReminderId { get; set; }

    [Required]
    public int MedicationId { get; set; }

    public string? MedicationName { get; set; }

    [Required]
    public TimeSpan Time { get; set; }

    [Required]
    public int Dosage { get; set; }

    [Required]
    public string RepeatDays { get; set; }

    public bool MedicationTaken { get; set; } = false;

}
