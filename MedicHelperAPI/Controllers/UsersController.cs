using AutoMapper;
using MedicHelperAPI.DTOs;
using MedicHelperAPI.Models;
using MedicHelperAPI.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;

namespace MedicHelperAPI.Controllers;

[Route("api/[controller]")]
[ApiController]
public class UsersController : ControllerBase
{
    private readonly MedicHelperContext _context;
    private readonly IAuthService _authService;
    private readonly IMapper _mapper;
    private readonly UserManager<User> _userManager;

    public UsersController(MedicHelperContext context, IAuthService authService, IMapper mapper, UserManager<User> userManager)
    {
        _context = context;
        _authService = authService;
        _mapper = mapper;
        _userManager = userManager;
    }

    [Authorize]
    [HttpGet]
    public async Task<IActionResult> GetUsers()
    {
        try
        {
            var users = await _context.Users.ToListAsync();
            return Ok(users);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { Message = "An unexpected error occurred.", Details = ex.Message });
        }
    }

    [Authorize]
    [HttpGet("me")]
    public async Task<IActionResult> GetUserProfile()
    {
        var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (string.IsNullOrEmpty(userId))
        {
            return BadRequest("User ID not found.");
        }

        var user = await _context.Users.FindAsync(userId);
        if (user == null)
        {
            return NotFound("User not found.");
        }

        var userDto = _mapper.Map<UserDTO>(user);
        return Ok(userDto);
    }

    [Authorize]
    [HttpPut("update")]
    public async Task<IActionResult> UpdateUser([FromBody] UpdateUserDTO updateUserDto)
    {
        if (updateUserDto == null)
        {
            return BadRequest("Invalid user data.");
        }

        var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (string.IsNullOrEmpty(userId))
        {
            return BadRequest("User ID not found.");
        }

        var user = await _userManager.FindByIdAsync(userId);
        if (user == null)
        {
            return NotFound("User not found.");
        }

        user.FirstName = updateUserDto.FirstName;
        user.LastName = updateUserDto.LastName;
        user.PhoneNumber = updateUserDto.PhoneNumber;

        if (user.Email != updateUserDto.Email)
        {
            user.Email = updateUserDto.Email;
            user.UserName = updateUserDto.Email; // Update UserName to be the new email
        }

        user.UpdatedAt = DateTime.UtcNow;

        var result = await _userManager.UpdateAsync(user);
        if (!result.Succeeded)
        {
            var errors = string.Join(", ", result.Errors.Select(e => e.Description));
            return BadRequest($"Update failed: {errors}");
        }

        return Ok("User updated successfully.");
    }

    [Authorize]
    [HttpPut("changePassword")]
    public async Task<IActionResult> ChangePasswordAsync(ChangePasswordDTO changePasswordDto)
    {
        if (changePasswordDto == null)
        {
            return BadRequest("Invalid password change data.");
        }

        try
        {
            var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
            var user = await _userManager.FindByIdAsync(userId);


            if(user.NormalizedEmail == changePasswordDto.Email.ToUpper())
            {
                var result = await _authService.ChangePasswordAsync(changePasswordDto);
                return Ok(new { Message = result });
            }
            else
            {
                return Unauthorized("Access Denied");
            }
            
        }
        catch (NotFoundException ex)
        {
            return NotFound(new { Message = ex.Message });
        }
        catch (IdentityException ex)
        {
            return BadRequest(new { Message = ex.Message, Errors = ex.Errors });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { Message = "An unexpected error occurred.", Details = ex.Message });
        }
    }

    [Authorize]
    [HttpPost("update-fcm-token")]
    public async Task<IActionResult> UpdateFCMToken([FromBody] string fcmToken)
    {
        var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
        var user = await _userManager.FindByIdAsync(userId);

        if (user == null)
        {
            return NotFound("User not found");
        }

        user.FCMToken = fcmToken; // Update the FCM token

        var result = await _userManager.UpdateAsync(user);

        if (!result.Succeeded)
        {
            return BadRequest(result.Errors);
        }

        return Ok("FCM token updated successfully");
    }

    [Authorize]
    [HttpPost("logout")]
    public async Task<IActionResult> Logout([FromBody] string fcmToken)
    {
        // Validate if the user is authenticated
        var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);

        var user = await _userManager.FindByIdAsync(userId);
        if (user == null)
            return NotFound("User not found");

        // Clear the provided FCM token if it matches the one in the database
        if (user.FCMToken == fcmToken)
        {
            user.FCMToken = ""; // Clear FCM token
            var result = await _userManager.UpdateAsync(user);

            if (!result.Succeeded)
            {
                return BadRequest(result.Errors);
            }

            return Ok("FCM token updated successfully");
        }
        else
        {
            return BadRequest("Provided FCM token does not match");
        }
    }


}
