using System;
using System.ComponentModel.DataAnnotations;

namespace MedicHelperAPI.Models;

public class Reminder
{
    public int ReminderId { get; set; }

    [Required]
    public int MedicationId { get; set; }

    [Required]
    public TimeSpan Time { get; set; }

    [Required]
    public int Dosage { get; set; }

    [Required]
    public string RepeatDays { get; set; }

    public bool MedicationTaken { get; set; } = false;
    public DateTime CreatedAt { get; set; }
    public DateTime? UpdatedAt { get; set; }

    // Navigation properties
    public Medication Medication { get; set; }
}
