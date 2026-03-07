using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Claims;
using System.Threading.Tasks;
using AutoMapper;
using MedicHelperAPI.DTOs;
using MedicHelperAPI.Models;
using MedicHelperAPI.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Newtonsoft.Json;

namespace MedicHelperAPI.Controllers;

[Authorize]
[ApiController]
[Route("api/[controller]")]
public class FamilyController : ControllerBase
{
    private readonly MedicHelperContext _context;
    private readonly IMapper _mapper;
    private readonly IJwtTokenGenerator _tokenService;
    private readonly ILogger<FamilyController> _logger;

    public FamilyController(MedicHelperContext context, IMapper mapper, IJwtTokenGenerator tokenService, ILogger<FamilyController> logger)
    {
        _context = context;
        _mapper = mapper;
        _tokenService = tokenService;
        _logger = logger;
    }

    [HttpPost("send-request")]
    public async Task<IActionResult> SendFamilyRequest([FromBody] AddFamilyMemberDTO model)
    {
        var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (userId == null)
            return Unauthorized();

        _logger.LogInformation($"User {userId} is sending a family request to {model.Email}");

        // Look up the user by email
        var requestedUser = await _context.Users.FirstOrDefaultAsync(u => u.Email == model.Email);
        if (requestedUser == null)
            return NotFound("Requested user not found.");

        // Check if a request already exists or if the family member is already added
        var existingRequest = await _context.Families
            .FirstOrDefaultAsync(f => f.UserId == userId && f.FamilyUserId == requestedUser.Id && !f.IsRemoved);

        if (existingRequest != null)
            return BadRequest("A request already exists or the family member is already added.");

        // Create a new family request
        var familyRequest = new Family
        {
            UserId = userId,
            FamilyUserId = requestedUser.Id,
            RequestedOn = DateTime.UtcNow,
            IsApproved = false,
            IsRemoved = false
        };

        _context.Families.Add(familyRequest);
        await _context.SaveChangesAsync();

        return Ok("Family member request sent successfully.");
    }


    [HttpPost("approve-request/{familyMemberId}")]
    public async Task<IActionResult> ApproveFamilyRequest(int familyMemberId)
    {
        var requestedUserId = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (requestedUserId == null)
            return Unauthorized();

        var familyRequest = await _context.Families
            .FirstOrDefaultAsync(f => f.FamilyMemberId == familyMemberId && f.FamilyUserId == requestedUserId);

        if (familyRequest == null)
            return NotFound("Family member request not found.");

        if (familyRequest.IsApproved)
            return BadRequest("This request has already been approved.");

        familyRequest.IsApproved = true;
        familyRequest.ApprovedOn = DateTime.UtcNow;

        _context.Families.Update(familyRequest);
        await _context.SaveChangesAsync();

        return Ok("Family member request approved successfully.");
    }

    [HttpPost("reject-request/{familyMemberId}")]
    public async Task<IActionResult> RejectFamilyRequest(int familyMemberId)
    {
        var requestedUserId = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (requestedUserId == null)
            return Unauthorized();

        var familyRequest = await _context.Families
            .FirstOrDefaultAsync(f => f.FamilyMemberId == familyMemberId && f.FamilyUserId == requestedUserId && !f.IsApproved && !f.IsRemoved);

        if (familyRequest == null)
            return NotFound("Family member request not found or already processed.");

        familyRequest.IsRemoved = true;
        familyRequest.UpdatedOn = DateTime.UtcNow;

        _context.Families.Update(familyRequest);
        await _context.SaveChangesAsync();

        return Ok("Family member request rejected successfully.");
    }

    [HttpGet("pending-requests")]
    public async Task<IActionResult> GetPendingRequests()
    {
        var requestedUserId = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (requestedUserId == null)
            return Unauthorized();

        var pendingRequests = await _context.Families
            .Where(f => f.FamilyUserId == requestedUserId && !f.IsApproved && !f.IsRemoved)
            .Include(f => f.User)
            .ToListAsync();

        var response = pendingRequests.Select(f => new
        {
            f.FamilyMemberId,
            RequestingUser = new
            {
                f.User.FirstName,
                f.User.LastName,
                f.User.Email
            },
            f.RequestedOn
        });

        return Ok(response);
    }

    [HttpGet("list")]
    public async Task<IActionResult> GetFamilyMembers()
    {
        var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (userId == null)
            return Unauthorized();

        var familyMembers = await _context.Families
            .Where(f => f.UserId == userId && f.IsApproved && !f.IsRemoved)
            .Include(f => f.FamilyUser)
            .ToListAsync();

        var familyMembersDTO = _mapper.Map<List<FamilyMemberDTO>>(familyMembers);
        return Ok(familyMembersDTO);
    }

    [HttpDelete("remove/{familyMemberId}")]
    public async Task<IActionResult> RemoveFamilyMember(int familyMemberId)
    {
        var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (userId == null)
            return Unauthorized();

        var family = await _context.Families
            .FirstOrDefaultAsync(f => f.FamilyMemberId == familyMemberId && f.UserId == userId);

        if (family == null)
            return NotFound("Family member relationship not found.");

        family.IsRemoved = true;
        family.UpdatedOn = DateTime.UtcNow;

        _context.Families.Update(family);
        await _context.SaveChangesAsync();

        return Ok("Family member removed successfully.");
    }

    [HttpGet("switch/{familyUserId}")]
    public async Task<IActionResult> SwitchToFamilyMember(string familyUserId)
    {
        var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (userId == null)
            return Unauthorized();

        var family = await _context.Families
            .FirstOrDefaultAsync(f => f.UserId == userId && f.FamilyUserId == familyUserId && f.IsApproved && !f.IsRemoved);

        if (family == null)
            return Unauthorized("You don't have access to this family member.");

        var familyUser = await _context.Users.FirstOrDefaultAsync(f => f.Id == familyUserId);

        if (familyUser == null)
            return NotFound("Family member not found.");

        var token = await _tokenService.GenerateTokenAsync(familyUser);
        var token1 = familyUser.FCMToken;


        return Ok(new { token,token1});
    }
}



