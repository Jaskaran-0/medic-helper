using AutoMapper;
using MedicHelperAPI.DTOs;
using MedicHelperAPI.Models;
using Microsoft.AspNetCore.Identity;
using System;

namespace MedicHelperAPI.Services;

public class AuthService : IAuthService
{
    private readonly UserManager<User> _userManager;
    private readonly IJwtTokenGenerator _jwtTokenGenerator;
    private readonly IMapper _mapper;

    public AuthService(UserManager<User> userManager, IJwtTokenGenerator jwtTokenGenerator, IMapper mapper)
    {
        _userManager = userManager;
        _jwtTokenGenerator = jwtTokenGenerator;
        _mapper = mapper;
    }

    public async Task<string> RegisterUserAsync(RegisterModelDTO registerDTO)
    {
        var user = _mapper.Map<User>(registerDTO);
        user.UserName = registerDTO.Email;
        user.Dob = DateTime.SpecifyKind(registerDTO.Dob, DateTimeKind.Utc);

        var result = await _userManager.CreateAsync(user, registerDTO.PasswordHash);

        if (result.Succeeded)
        {
            return await _jwtTokenGenerator.GenerateTokenAsync(user);
        }

        throw new IdentityException("Registration failed", result.Errors);
    }

    public async Task<string> LoginUserAsync(LoginModelDTO loginDTO)
    {
        var user = await _userManager.FindByEmailAsync(loginDTO.Email.ToUpper());
        if (user == null)
        {
            throw new NotFoundException("User not found.");
        }

        // SECURITY FIX: Removed the fingerprint bypass block that previously returned a
        // token based solely on client-sent flags (FingerprintEnabled + FingerprintLoginAuth).
        // The server cannot trust those claims — biometric auth is handled on the device and
        // the result must NOT skip server-side password verification. Every login now requires
        // the password to be checked, regardless of fingerprint preference.
        if (!await _userManager.CheckPasswordAsync(user, loginDTO.PasswordHash))
        {
            throw new UnauthorizedAccessException("Wrong password!");
        }

        return await _jwtTokenGenerator.GenerateTokenAsync(user);
    }

    public async Task<bool> ValidateTokenAsync(string token)
    {
        return await _jwtTokenGenerator.ValidateTokenAsync(token);
    }

    public async Task<string> ChangePasswordAsync(ChangePasswordDTO changePasswordDto)
    {
        var user = await _userManager.FindByEmailAsync(changePasswordDto.Email.ToUpper());
        if (user == null)
        {
            throw new NotFoundException("User not found.");
        }

        var result = await _userManager.ChangePasswordAsync(user, changePasswordDto.CurrentPassword, changePasswordDto.NewPassword);
        if (!result.Succeeded)
        {
            throw new IdentityException("Password change failed", result.Errors);
        }

        // Update fingerprint login preference if provided
        if (changePasswordDto.FingerprintEnabled.HasValue  && changePasswordDto.FingerprintEnabled != user.FingerprintEnabled)
        {
            user.FingerprintEnabled = changePasswordDto.FingerprintEnabled.Value;
            user.UpdatedAt = DateTime.UtcNow;
            var updateResult = await _userManager.UpdateAsync(user);
            if (!updateResult.Succeeded)
            {
                throw new Exception("Failed to update fingerprint preference.");
            }
        }

        return "Password changed successfully.";
    }
}

// Custom exceptions
public class IdentityException : Exception
{
    public IEnumerable<IdentityError> Errors { get; }

    public IdentityException(string message, IEnumerable<IdentityError> errors)
        : base(message)
    {
        Errors = errors;
    }
}

public class NotFoundException : Exception
{
    public NotFoundException(string message) : base(message) { }
}
