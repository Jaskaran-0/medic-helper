using AutoMapper;
using MedicHelperAPI.DTOs;
using MedicHelperAPI.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;

namespace MedicHelperAPI.Controllers;

[Route("api/[controller]")]
[ApiController]
public class MedicationsController : ControllerBase
{
    private readonly IMapper _mapper;
    private readonly MedicHelperContext _context;

    public MedicationsController(IMapper mapper, MedicHelperContext context)
    {
        _mapper = mapper;
        _context = context;
    }

    [Authorize]
    [HttpGet]
    public async Task<ActionResult<IEnumerable<MedicationDTO>>> GetMedications()
    {
        try
        {
            var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
            var medications = await _context.Medications.Include(m => m.Reminders).Where(m=> m.UserId == userId).ToListAsync();
            var medicationDTOs = _mapper.Map<List<MedicationDTO>>(medications);
            return Ok(medicationDTOs);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { Message = "An unexpected error occurred.", Details = ex.Message });
        }
    }

    [Authorize]
    [HttpPost]
    public async Task<ActionResult<MedicationDTO>> AddMedication([FromBody] MedicationDTO medicationDTO)
    {
        if (medicationDTO == null)
        {
            return BadRequest(new { Message = "Invalid medication data." });
        }

        try
        {
            var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (userId == null)
            {
                return Unauthorized(new { Message = "User is not authenticated." });
            }

            var medication = _mapper.Map<Medication>(medicationDTO);
            medication.UserId = userId;
            medication.UpdatedAt = null;

            await _context.Medications.AddAsync(medication);
            await _context.SaveChangesAsync();

            var createdMedicationDto = _mapper.Map<MedicationDTO>(medication);
            return CreatedAtAction(nameof(GetMedications), new { id = medication.MedicationId }, createdMedicationDto);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { Message = "An error occurred while adding the medication.", Details = ex.Message });
        }
    }

    [Authorize]
    [HttpPut]
    public async Task<IActionResult> UpdateMedication([FromBody] MedicationDTO medicationDto)
    {
        if (medicationDto == null)
        {
            return BadRequest(new { Message = "Invalid medication data." });
        }

        try
        {
            var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);

            // SECURITY FIX: Previously this checked if the user owned *any* medication
            // (medications.Count != 0), then fetched the target medication by ID without
            // verifying ownership. Any user with at least one medication could update
            // any other user's medication. Now we fetch only if UserId matches.
            var medication = await _context.Medications
                .FirstOrDefaultAsync(m => m.MedicationId == medicationDto.MedicationId && m.UserId == userId);

            if (medication == null)
            {
                // Returns 404 regardless of whether the medication exists but belongs to
                // someone else — avoids leaking information about other users' data.
                return NotFound(new { Message = "Medication not found." });
            }

            medication.Inventory = medicationDto.Inventory;
            medication.UpdatedAt = DateTime.UtcNow;

            _context.Medications.Update(medication);
            await _context.SaveChangesAsync();
            return NoContent();
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { Message = "An error occurred while updating the medication.", Details = ex.Message });
        }
    }

    [Authorize]
    [HttpDelete("{medicationId}")]
    public async Task<IActionResult> Delete(int medicationId)
    {
        try
        {
            var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);

            // SECURITY FIX: Previously fetched by ID only with no ownership check, allowing
            // any authenticated user to delete any medication by guessing its ID.
            // Now filters by both medicationId AND the current user's ID.
            var medication = await _context.Medications
                .FirstOrDefaultAsync(m => m.MedicationId == medicationId && m.UserId == userId);

            if (medication == null)
            {
                return NotFound(new { Message = "Medication not found." });
            }

            _context.Medications.Remove(medication);
            await _context.SaveChangesAsync();

            return NoContent();
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { Message = "An error occurred while deleting the medication.", Details = ex.Message });
        }
    }
}
