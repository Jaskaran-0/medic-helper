namespace MedicHelperAPI.Services;

using FirebaseAdmin.Messaging;
using System.Threading.Tasks;

public class NotificationService
{
    private readonly ILogger<NotificationService> _logger;

    // FIX: Added ILogger injection. The original code used Console.WriteLine which bypasses
    // the ASP.NET Core logging pipeline (no log levels, no structured logging, no sinks).
    // ILogger integrates with the configured providers (console, Azure Monitor, etc.).
    public NotificationService(ILogger<NotificationService> logger)
    {
        _logger = logger;
    }

    public async Task SendNotificationAsync(string fcmToken, string title, string body)
    {
        var message = new Message()
        {
            Token = fcmToken,
            Notification = new Notification()
            {
                Title = title,
                Body = body
            }
        };

        string response = await FirebaseMessaging.DefaultInstance.SendAsync(message);
        _logger.LogInformation("Successfully sent FCM message: {Response}", response);
    }
}
