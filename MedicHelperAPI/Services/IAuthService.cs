using MedicHelperAPI.DTOs;

namespace MedicHelperAPI.Services;

public interface IAuthService
{
    Task<string> RegisterUserAsync(RegisterModelDTO registerDTO);
    Task<string> LoginUserAsync(LoginModelDTO loginDTO);
    Task<bool> ValidateTokenAsync(string token);
    Task<string> ChangePasswordAsync(ChangePasswordDTO changePasswordDTO);
}
