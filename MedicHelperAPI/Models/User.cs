using Microsoft.AspNetCore.Identity;
using System;
using System.Collections.Generic;

namespace MedicHelperAPI.Models;

public class User : IdentityUser
{
    public string FirstName { get; set; }
    public string LastName { get; set; }
    public DateTime Dob { get; set; }
    public bool FingerprintEnabled { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime? UpdatedAt { get; set; }

    public string FCMToken { get; set; }

    // Navigation properties
    public ICollection<Note> Notes { get; set; }
    public ICollection<Appointment> Appointments { get; set; }
    public ICollection<Medication> Medications { get; set; }
    public ICollection<Family> Families { get; set; }
}
