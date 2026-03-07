using System.ComponentModel.DataAnnotations;

namespace MedicHelperAPI.DTOs;
public class CreateNoteDTO
{
    [Required]
    public string Title { get; set; }

    [Required]
    public string NoteContent { get; set; }

    public byte[]? Image { get; set; }
}