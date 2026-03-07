namespace MedicHelperAPI.Services;

using FirebaseAdmin.Messaging;
using System.Threading.Tasks;

public class NotificationService
{
    // Method to send notification
    public async Task SendNotificationAsync(string fcmToken, string title, string body)
    {
        // Create a message to be sent
        var message = new Message()
        {
            Token = fcmToken,
            Notification = new Notification()
            {
                Title = title,
                Body = body
            }
        };

        // Send the message
        string response = await FirebaseMessaging.DefaultInstance.SendAsync(message);
        Console.WriteLine("Successfully sent message: " + response);
    }
}
