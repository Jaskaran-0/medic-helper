using System.Security.Claims;
using System.Threading.Tasks;
using AutoMapper;
using MedicHelperAPI.DTOs;
using MedicHelperAPI.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace MedicHelperAPI.Controllers;

[Route("api/[controller]")]
[ApiController]
[Authorize]
public class NotesController : ControllerBase
{
    private readonly MedicHelperContext _context;
    private readonly IMapper _mapper;

    public NotesController(MedicHelperContext context, IMapper mapper)
    {
        _context = context;
        _mapper = mapper;
    }

    [HttpGet]
    public async Task<ActionResult<IEnumerable<NoteDTO>>> GetNotes()
    {
        var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
        var notes = await _context.Notes
            .Where(n => n.UserId == userId)
            .OrderByDescending(n => n.CreatedAt)
            .ToListAsync();

        return Ok(_mapper.Map<List<NoteDTO>>(notes));
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<NoteDTO>> GetNoteById(int id)
    {
        var note = await _context.Notes.FindAsync(id);

        if (note == null || note.UserId != User.FindFirstValue(ClaimTypes.NameIdentifier))
        {
            return NotFound("Note not found or access denied.");
        }

        return Ok(_mapper.Map<NoteDTO>(note));
    }

    [HttpPost]
    public async Task<ActionResult<NoteDTO>> CreateNote([FromBody] CreateNoteDTO createNoteDTO)
    {
        var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
        var note = _mapper.Map<Note>(createNoteDTO);
        note.UserId = userId;

        _context.Notes.Add(note);
        await _context.SaveChangesAsync();

        return CreatedAtAction(nameof(GetNoteById), new { id = note.NoteId }, _mapper.Map<NoteDTO>(note));
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteNote(int id)
    {
        var note = await _context.Notes.FindAsync(id);
        if (note == null || note.UserId != User.FindFirstValue(ClaimTypes.NameIdentifier))
        {
            return NotFound("Note not found or access denied.");
        }

        _context.Notes.Remove(note);
        await _context.SaveChangesAsync();

        return NoContent();
    }
}
