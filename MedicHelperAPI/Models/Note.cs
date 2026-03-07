using System;

namespace MedicHelperAPI.Models
{
    public class Note
    {
        public int NoteId { get; set; }
        public string UserId { get; set; }
        public string Title { get; set; }
        public string NoteContent { get; set; }
        public byte[] Image { get; set; }
        public DateTime CreatedAt { get; set; }

        // Navigation properties
        public User User { get; set; }
    }
}
