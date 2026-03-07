namespace MedicHelperAPI.DTOs;
public class NoteDTO
{
    public int NoteId { get; set; }
    public string Title { get; set; }
    public string NoteContent { get; set; }
    public byte[] Image { get; set; }
    public DateTime CreatedAt { get; set; }
}