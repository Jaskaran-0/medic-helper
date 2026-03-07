using System;

namespace MedicHelperAPI.Models
{
    public class Appointment
    {
        public int AppointmentId { get; set; }
        public string UserId { get; set; }
        public DateTime Date { get; set; }
        public TimeSpan Time { get; set; }
        public string Title { get; set; }
        public string Description { get; set; }
        public DateTime CreatedAt { get; set; }
        public DateTime? UpdatedAt { get; set; }
        public bool IsDeleted { get; set; }

        // Navigation properties
        public User User { get; set; }
    }
}
