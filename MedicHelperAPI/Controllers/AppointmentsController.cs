using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using MedicHelperAPI.DTOs;
using MedicHelperAPI.Models;
using AutoMapper;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;

namespace MedicHelperAPI.Controllers;

[Authorize]
[Route("api/[controller]")]
[ApiController]
public class AppointmentsController : ControllerBase
{
    private readonly MedicHelperContext _context;
    private readonly IMapper _mapper;

    public AppointmentsController(MedicHelperContext context, IMapper mapper)
    {
        _context = context;
        _mapper = mapper;
    }

    [HttpGet]
    public async Task<ActionResult<IEnumerable<AppointmentDTO>>> GetAppointments()
    {
        var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
        var appointments = await _context.Appointments
        .Where(a => a.UserId == userId && !a.IsDeleted && a.Date > DateTime.UtcNow.Date)
        .OrderBy(a => a.Date) // Order by date first
        .ThenBy(a => a.Time)  // Then order by time within each date
        .ToListAsync();


        return Ok(_mapper.Map<List<AppointmentDTO>>(appointments));
    }

    [HttpPost]
    public async Task<IActionResult> AddAppointment([FromBody] CreateAppointmentDTO appointmentDTO)
    {
        if (appointmentDTO == null)
            return BadRequest("Invalid appointment data.");

        var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);

        if (userId == null)
        {
            return Unauthorized(new { Message = "User is not authenticated." });
        }

        // FIX: Added validation that the appointment date is in the future.
        // Without this check users could create appointments in the past, which
        // would never appear in the list (GetAppointments filters by Date > today)
        // and would still trigger notification checks in ReminderService.
        if (appointmentDTO.Date.Date <= DateTime.UtcNow.Date)
        {
            return BadRequest("Appointment date must be in the future.");
        }

        var appointment = _mapper.Map<Appointment>(appointmentDTO);

        appointment.UserId = userId;
        appointment.CreatedAt = DateTime.UtcNow;

        _context.Appointments.Add(appointment);
        await _context.SaveChangesAsync();

        return CreatedAtAction(nameof(GetAppointments), new { id = appointment.AppointmentId }, appointment);
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteAppointment(int id)
    {
        var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
        var appointment = await _context.Appointments.FirstOrDefaultAsync(a => a.AppointmentId == id && a.UserId == userId);

        if (appointment == null)
            return NotFound();

        appointment.IsDeleted = true;
        appointment.UpdatedAt = DateTime.UtcNow;
        await _context.SaveChangesAsync();
        return NoContent();
    }
}
