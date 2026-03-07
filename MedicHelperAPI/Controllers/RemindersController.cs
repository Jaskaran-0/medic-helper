using AutoMapper;
using MedicHelperAPI.DTOs;
using MedicHelperAPI.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Collections.Generic;
using System.Security.Claims;
using System.Threading.Tasks;

namespace MedicHelperAPI.Controllers;

[Route("api/[controller]")]
[ApiController]
public class RemindersController : ControllerBase
{
    private readonly MedicHelperContext _context;
    private readonly IMapper _mapper;
    private static readonly TimeZoneInfo EasternTimeZone = TimeZoneInfo.FindSystemTimeZoneById("Eastern Standard Time");


    public RemindersController(MedicHelperContext context, IMapper mapper)
    {
        _context = context;
        _mapper = mapper;
    }

    [Authorize]
    [HttpGet]
    public async Task<ActionResult<IEnumerable<ReminderDTO>>> GetReminders()
    {
        var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);

        var nowUtc = DateTime.UtcNow;
        var nowEst = TimeZoneInfo.ConvertTimeFromUtc(nowUtc, EasternTimeZone);
        var today = nowEst.DayOfWeek.ToString().Substring(0, 3);

        var reminders = await _context.Reminders
            .Include(r => r.Medication)
            .Where(r => r.Medication.UserId == userId && r.RepeatDays.Contains(today))
            .OrderBy(r => r.Time)  // Sort by Time ascending
            .ToListAsync();

        return Ok(_mapper.Map<List<ReminderDTO>>(reminders));
    }


    [Authorize]
    [HttpPost]
    public async Task<ActionResult<ReminderDTO>> AddReminder([FromBody] ReminderDTO reminderDTO)
    {
        if (reminderDTO == null)
        {
            return BadRequest("Invalid reminder data.");
        }

        var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
        var medications = await _context.Medications.Where(m=> m.MedicationId == reminderDTO.MedicationId && m.UserId==userId).ToListAsync();

        if(medications.Count() == 0)
        {
            return Unauthorized("Cant set reminder on this medication");
        }

        if(reminderDTO.Dosage > medications[0].Inventory && reminderDTO.Dosage <=0)
        {
            return BadRequest("Dosage is greater than the medication dosage");
        }

        var reminder = _mapper.Map<Reminder>(reminderDTO);
        reminder.CreatedAt = DateTime.UtcNow;
        _context.Reminders.Add(reminder);
        await _context.SaveChangesAsync();

        reminderDTO.ReminderId = reminder.ReminderId;
        reminderDTO.MedicationName= medications[0].Name;

        return CreatedAtAction(nameof(GetReminders), new { id = reminder.ReminderId }, reminderDTO);
    }

    [Authorize]
    [HttpPut]
    public async Task<IActionResult> UpdateReminder([FromBody] UpdateReminderDTO reminderDTO)
    {
        if (reminderDTO == null)
        {
            return BadRequest("Invalid reminder data.");
        }

        var reminder = await _context.Reminders.FindAsync(reminderDTO.ReminderId);
        
        if (reminder == null)
        {
            return NotFound("Reminder not found.");
        }

        var validityResult = await userValidity(reminder);
        if (validityResult is UnauthorizedResult)
        {
            return Unauthorized("Access Denied!");
        }

        var medication = await _context.Medications.FindAsync(reminder.MedicationId);

        if (reminderDTO.Dosage > medication.Inventory)
        {
            medication.Inventory = 0;
        }
        else
        {
            medication.Inventory -= reminderDTO.Dosage;
        }

        _mapper.Map(reminderDTO, reminder);
        reminder.UpdatedAt = DateTime.UtcNow;
        reminder.MedicationTaken= reminderDTO.MedicationTaken;

        await _context.SaveChangesAsync();
        return NoContent();
    }

    [Authorize]
    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteReminder(int id)
    {
        var reminder = await _context.Reminders.FindAsync(id);
        if (reminder == null)
        {
            return NotFound("Reminder not found.");
        }

        var validityResult = await userValidity(reminder);
        if (validityResult is UnauthorizedResult) 
        {
            return Unauthorized("Access Denied!");
        }

        _context.Reminders.Remove(reminder);
        await _context.SaveChangesAsync();
        return NoContent();
    }

    
    private async Task<ActionResult> userValidity(Reminder reminder)
    {
        var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
        var hasAccess = await _context.Medications
            .AnyAsync(m => m.MedicationId == reminder.MedicationId && m.UserId == userId);

        if (!hasAccess)
        {
            return Unauthorized();
        }

        return Ok();
    }
}
