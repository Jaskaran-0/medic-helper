using System.ComponentModel.DataAnnotations;

namespace MedicHelperAPI.DTOs;

public class MedicationDTO
{
    [Required]
    public int MedicationId { get; set; }

    [Required]
    public string Name { get; set; }

    public int Inventory { get; set; }
    public byte[]? Image { get; set; }
}
