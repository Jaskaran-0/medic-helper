using MedicHelperAPI.DTOs;
using MedicHelperAPI.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System;

namespace MedicHelperAPI.Controllers;

[Route("api/[controller]")]
[ApiController]
public class AuthController : ControllerBase
{
    private readonly IAuthService _authService;

    public AuthController(IAuthService authService)
    {
        _authService = authService;
    }

    [HttpPost("register")]
    [AllowAnonymous]
    public async Task<IActionResult> Register([FromBody] RegisterModelDTO registerModelDTO)
    {
        if (registerModelDTO == null)
        {
            return BadRequest("Invalid registration data.");
        }

        try
        {
            var token = await _authService.RegisterUserAsync(registerModelDTO);
            return Ok(new { Token = token });
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

    [HttpPost("login")]
    [AllowAnonymous]
    public async Task<IActionResult> Login([FromBody] LoginModelDTO loginDTO)
    {
        if (loginDTO == null)
        {
            return BadRequest("Invalid login data.");
        }

        try
        {
            var token = await _authService.LoginUserAsync(loginDTO);
            return Ok(new { Token = token });
        }
        catch (NotFoundException ex)
        {
            return NotFound(new { Message = ex.Message });
        }
        catch (IdentityException ex)
        {
            return BadRequest(new { Message = ex.Message, Errors = ex.Errors });
        }
        catch (UnauthorizedAccessException ex)
        {
            return Unauthorized(new { Message = ex.Message });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { Message = "An unexpected error occurred.", Details = ex.Message });
        }
    }

    [HttpPost("tokenValidityCheck")]
    [AllowAnonymous]
    public async Task<IActionResult> ValidateTokenAsync([FromBody] string token)
    {
        if (string.IsNullOrEmpty(token))
        {
            return BadRequest("Token cannot be null or empty.");
        }

        try
        {
            var validity = await _authService.ValidateTokenAsync(token);
            return Ok(new { IsValid = validity });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { Message = "An unexpected error occurred.", Details = ex.Message });
        }
    }
}
