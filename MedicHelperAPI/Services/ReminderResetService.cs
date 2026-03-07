using MedicHelperAPI.Models;
using Microsoft.EntityFrameworkCore;

namespace MedicHelperAPI.Services;

public class ReminderResetService :BackgroundService 
{
    private readonly IServiceScopeFactory _serviceScopeFactory;
    private readonly ILogger<ReminderResetService> _logger;

    public ReminderResetService(IServiceScopeFactory serviceScopeFactory, ILogger<ReminderResetService> logger)
    {
        _serviceScopeFactory = serviceScopeFactory;
        _logger = logger;
    }

    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        while (!stoppingToken.IsCancellationRequested)
        {
            var now = DateTime.UtcNow;
            var timeUntilMidnight = now.Date.AddDays(1) - now;

            // Wait until midnight
            await Task.Delay(timeUntilMidnight, stoppingToken);

            using (var scope = _serviceScopeFactory.CreateScope())
            {
                var context = scope.ServiceProvider.GetRequiredService<MedicHelperContext>();
                var reminders = await context.Reminders.ToListAsync();

                foreach (var reminder in reminders)
                {
                        reminder.MedicationTaken = false;
                        await context.SaveChangesAsync();
                }
            }
        }
    }
}
