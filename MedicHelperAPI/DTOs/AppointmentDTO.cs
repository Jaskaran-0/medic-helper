namespace MedicHelperAPI.DTOs;

public class AppointmentDTO
{
    public int AppointmentId { get; set; }
    public string Title { get; set; }
    public string Description { get; set; }
    public DateTime Date { get; set; }
    public TimeSpan Time { get; set; }
    public bool IsDeleted { get; set; }
}
