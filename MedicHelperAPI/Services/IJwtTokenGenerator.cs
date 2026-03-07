using MedicHelperAPI.Models;

namespace MedicHelperAPI.Services
{
    public interface IJwtTokenGenerator
    {
        Task<string> GenerateTokenAsync(User user);
        Task<bool> ValidateTokenAsync(string token);
    }
}
