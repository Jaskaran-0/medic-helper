using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace MedicHelperAPI.Models
{
    public class Medication
    {
        [Required]
        public int MedicationId { get; set; }

        [Required]
        public string UserId { get; set; }

        [Required]
        public string Name { get; set; }
        public int Inventory { get; set; }
        public byte[] Image { get; set; }
        public DateTime CreatedAt { get; set; }
        public DateTime? UpdatedAt { get; set; }

        // Navigation properties
        public User User { get; set; }
        public ICollection<Reminder> Reminders { get; set; }
    }
}
