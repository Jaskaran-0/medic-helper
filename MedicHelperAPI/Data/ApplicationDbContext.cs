using Microsoft.AspNetCore.Identity.EntityFrameworkCore;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;

namespace MedicHelperAPI.Models
{
    public class MedicHelperContext : IdentityDbContext<User>
    {
        public MedicHelperContext(DbContextOptions<MedicHelperContext> options) : base(options)
        {
        }

        public DbSet<Medication> Medications { get; set; }
        public DbSet<Reminder> Reminders { get; set; }
        public DbSet<Appointment> Appointments { get; set; }
        public DbSet<Note> Notes { get; set; }
        public DbSet<Family> Families { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            modelBuilder.Entity<User>().ToTable("Users");
            modelBuilder.Entity<Medication>().ToTable("Medications");
            modelBuilder.Entity<Reminder>().ToTable("Reminders");
            modelBuilder.Entity<Appointment>().ToTable("Appointments");
            modelBuilder.Entity<Note>().ToTable("Notes");
            modelBuilder.Entity<Family>().ToTable("Families");

            modelBuilder.Entity<User>()
                .HasKey(u => u.Id);

            modelBuilder.Entity<Medication>()
                .HasKey(m => m.MedicationId);

            modelBuilder.Entity<Reminder>()
                .HasKey(r => r.ReminderId);

            modelBuilder.Entity<Appointment>()
                .HasKey(a => a.AppointmentId);

            modelBuilder.Entity<Note>()
                .HasKey(n => n.NoteId);

            modelBuilder.Entity<Family>()
                .HasKey(f => f.FamilyMemberId);

            modelBuilder.Entity<User>()
                .HasMany(u => u.Notes)
                .WithOne(n => n.User)
                .HasForeignKey(n => n.UserId);

            modelBuilder.Entity<User>()
                .HasMany(u => u.Appointments)
                .WithOne(a => a.User)
                .HasForeignKey(a => a.UserId);

            modelBuilder.Entity<User>()
                .HasMany(u => u.Medications)
                .WithOne(m => m.User)
                .HasForeignKey(m => m.UserId);

            modelBuilder.Entity<Family>()
                 .HasOne(f => f.User)
                 .WithMany(u => u.Families)
                 .HasForeignKey(f => f.UserId)
                 .OnDelete(DeleteBehavior.Restrict); // Prevent cascading delete when the user is deleted

            modelBuilder.Entity<Family>()
                .HasOne(f => f.FamilyUser)
                .WithMany()
                .HasForeignKey(f => f.FamilyUserId)
                .OnDelete(DeleteBehavior.Restrict); // Prevent cascading delete when the family member is deleted


            modelBuilder.Entity<Medication>()
                .HasMany(m => m.Reminders)
                .WithOne(r => r.Medication)
                .HasForeignKey(r => r.MedicationId);
        }
    }
}
