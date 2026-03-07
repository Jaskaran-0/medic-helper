using System.ComponentModel.DataAnnotations;

namespace MedicHelperAPI.DTOs;

public class CreateAppointmentDTO
{
    [Required]
    public string Title { get; set; }

    [Required]
    public string Description { get; set; }

    [Required]
    [DataType(DataType.Date)]
    public DateTime Date { get; set; }

    [Required]
    [DataType(DataType.Time)]
    public TimeSpan Time { get; set; }
}
