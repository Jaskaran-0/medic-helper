using MedicHelperAPI.Models;
using Microsoft.EntityFrameworkCore;
using MedicHelperAPI.Services;

public class ReminderService : BackgroundService
{
    private readonly IServiceScopeFactory _serviceScopeFactory;
    private readonly ILogger<ReminderService> _logger;
    // NOTE (timezone): Hardcoded to Eastern Standard Time. Users in other timezones will
    // receive reminders at incorrect local times. Future fix: store user timezone in the
    // User model and convert per-user when sending notifications.
    private static readonly TimeZoneInfo EasternTimeZone = TimeZoneInfo.FindSystemTimeZoneById("Eastern Standard Time");

    public ReminderService(IServiceScopeFactory serviceScopeFactory, ILogger<ReminderService> logger)
    {
        _serviceScopeFactory = serviceScopeFactory;
        _logger = logger;
    }

    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        while (!stoppingToken.IsCancellationRequested)
        {
            using (var scope = _serviceScopeFactory.CreateScope())
            {
                var context = scope.ServiceProvider.GetRequiredService<MedicHelperContext>();
                var notificationService = scope.ServiceProvider.GetRequiredService<NotificationService>();

                try
                {
                    // Convert UTC to EST
                    var nowUtc = DateTime.UtcNow;
                    var nowEst = TimeZoneInfo.ConvertTimeFromUtc(nowUtc, EasternTimeZone);
                    var today = nowEst.DayOfWeek.ToString().Substring(0, 3);
                    var currentMinute = new TimeSpan(nowEst.Hour, nowEst.Minute, 0);

                    // Fetch reminders for today within the current minute
                    var reminders = await context.Reminders
                        .Include(r => r.Medication)
                        .Where(r => r.RepeatDays.Contains(today) &&
                                    r.Time.Hours == currentMinute.Hours &&
                                    r.Time.Minutes == currentMinute.Minutes)
                        .ToListAsync(stoppingToken);

                    _logger.LogInformation($"Checking reminders at {nowEst}, found {reminders.Count}");

                    foreach (var reminder in reminders)
                    {
                        var user = await context.Users.FindAsync(reminder.Medication.UserId);
                        if (user == null)
                        {
                            _logger.LogWarning($"User with ID {reminder.Medication.UserId} not found");
                            continue;
                        }

                        if (string.IsNullOrEmpty(user.FCMToken))
                        {
                            _logger.LogInformation($"Skipping notification for user {user.Id} as FCMToken is not set");
                            continue;
                        }

                        try
                        {
                            await notificationService.SendNotificationAsync(
                                user.FCMToken,
                                $"Time to take your medication: {reminder.Medication.Name}",
                                $"{reminder.Dosage} of {reminder.Medication.Name}");
                            _logger.LogInformation($"Sent reminder to user {user.Id}: {reminder.Dosage} of {reminder.Medication.Name} at {nowEst}");
                        }
                        catch (Exception ex)
                        {
                            _logger.LogError(ex, $"Failed to send reminder to user {user.Id}");
                        }
                    }

                    // Check for upcoming appointments — notify 30 minutes before.
                    // FIX: Previously the date range used nowUtc.Date (UTC) while the time window
                    // used appointmentReminderTime derived from nowEst (Eastern). This caused the
                    // notification to fire at the wrong UTC time in some cases.
                    // Now both the date range and the time comparison use the same EST-based time.
                    var appointmentReminderTime = nowEst.AddMinutes(30);

                    var upcomingAppointments = await context.Appointments
                        .Where(a => a.Date > nowUtc.Date &&
                                    a.Date <= nowUtc.AddDays(1).Date &&
                                    a.Time.Hours == appointmentReminderTime.Hour &&
                                    a.Time.Minutes == appointmentReminderTime.Minute &&
                                    !a.IsDeleted)
                        .ToListAsync(stoppingToken);

                    _logger.LogInformation($"Checking appointments at {appointmentReminderTime}, found {upcomingAppointments.Count}");

                    foreach (var appointment in upcomingAppointments)
                    {
                        var user = await context.Users.FindAsync(appointment.UserId);
                        if (user == null)
                        {
                            _logger.LogWarning($"User with ID {appointment.UserId} not found");
                            continue;
                        }

                        if (string.IsNullOrEmpty(user.FCMToken))
                        {
                            _logger.LogInformation($"Skipping notification for user {user.Id} as FCMToken is not set");
                            continue;
                        }

                        try
                        {
                            await notificationService.SendNotificationAsync(
                                user.FCMToken,
                                $"Upcoming appointment: {appointment.Title}",
                                $"You have an appointment for {appointment.Description} at {appointment.Time}");
                            _logger.LogInformation($"Sent appointment reminder to user {user.Id}: {appointment.Title} at {nowEst}");
                        }
                        catch (Exception ex)
                        {
                            _logger.LogError(ex, $"Failed to send appointment reminder to user {user.Id}");
                        }
                    }
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "An error occurred while executing the ReminderService");
                }
            }

            // Wait 1 minute before the next check
            await Task.Delay(TimeSpan.FromMinutes(1), stoppingToken);
        }
    }
}
