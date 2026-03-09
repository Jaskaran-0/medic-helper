using Microsoft.AspNetCore.Builder;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using System.Text;
using MedicHelperAPI.Models;
using AutoMapper;
using MedicHelperAPI.Services;
using MedicHelperAPI.Mappings;

using Azure.Identity;
using Azure.Security.KeyVault.Secrets;

using FirebaseAdmin;
using Google.Apis.Auth.OAuth2;

namespace MedicHelperAPI;

public class Program
{
    public static void Main(string[] args)
    {
        var builder = WebApplication.CreateBuilder(args);

        var jwtKey = builder.Configuration["JWT:Key"];
        var jwtIssuer = builder.Configuration["JWT:Issuer"];
        var jwtAudience = builder.Configuration["JWT:Audience"];
        var firebaseAdminSdk = builder.Configuration["Firebase:AdminSDK"];
        var connectionString = builder.Configuration.GetConnectionString("DefaultConnection");

        var inMemorySettings = new Dictionary<string, string?>
        {
            { "Jwt:Key", jwtKey },
            { "Jwt:Issuer", jwtIssuer },
            { "Jwt:Audience", jwtAudience },
            { "ConnectionStrings:DefaultConnection", connectionString }
        };

        // Add secrets to configuration
        builder.Configuration.AddInMemoryCollection(inMemorySettings);

        builder.Services.AddHostedService<ReminderService>();
        builder.Services.AddHostedService<ReminderResetService>();

        builder.Services.AddDbContext<MedicHelperContext>(options =>
            options.UseNpgsql(connectionString));

        builder.Services.AddIdentity<User, IdentityRole>()
            .AddEntityFrameworkStores<MedicHelperContext>()
            .AddDefaultTokenProviders();

        builder.Services.Configure<IdentityOptions>(options =>
        {
            options.Password.RequireDigit = true;
            options.Password.RequiredLength = 8;
        });

        builder.Services.AddAuthentication(options =>
        {
            options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
            options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
        })
        .AddJwtBearer(options =>
        {
            options.TokenValidationParameters = new TokenValidationParameters
            {
                ValidateIssuer = true,
                ValidateAudience = true,
                ValidateLifetime = true,
                ValidateIssuerSigningKey = true,
                ValidIssuer = jwtIssuer,
                ValidAudience = jwtAudience,
                IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwtKey))
            };
        });

        using var secretStream = new MemoryStream(System.Text.Encoding.UTF8.GetBytes(firebaseAdminSdk));
        FirebaseApp.Create(new AppOptions
        {
            Credential = GoogleCredential.FromFile(firebaseAdminSdk)
        });

        builder.Services.AddAutoMapper(typeof(MappingProfile));
        builder.Services.AddControllers();
        builder.Services.AddScoped<IAuthService, AuthService>();
        builder.Services.AddScoped<IJwtTokenGenerator, JwtTokenGenerator>();
        // FIX: NotificationService now requires ILogger<NotificationService> — DI resolves
        // this automatically since ILogger is registered by AddLogging (called internally
        // by WebApplication.CreateBuilder). No extra registration needed here.
        builder.Services.AddScoped<NotificationService>();

        // FIX: Added Swagger/OpenAPI support. Swashbuckle.AspNetCore was already referenced
        // in the csproj but never wired up, making the API impossible to browse or test
        // without a separate client. Swagger UI is enabled in all environments here;
        // restrict to Development only if you don't want it exposed in production.
        builder.Services.AddEndpointsApiExplorer();
        builder.Services.AddSwaggerGen();

        // FIX: Added CORS policy. Without this, browser-based clients (including the Swagger
        // UI when served from a different origin) will be blocked by the browser's same-origin
        // policy. The policy below is permissive for development; tighten AllowAnyOrigin()
        // to specific origins before deploying to production.
        builder.Services.AddCors(options =>
        {
            options.AddPolicy("AllowAll", policy =>
            {
                policy.AllowAnyOrigin()
                      .AllowAnyHeader()
                      .AllowAnyMethod();
            });
        });

        var app = builder.Build();

        using (var scope = app.Services.CreateScope())
        {
            var db = scope.ServiceProvider.GetRequiredService<MedicHelperContext>();
            db.Database.Migrate();
        }

        if (app.Environment.IsDevelopment())
        {
            app.UseDeveloperExceptionPage();
        }
        else
        {
            app.UseExceptionHandler("/Home/Error");
            app.UseHsts();
        }

        // FIX: Swagger middleware added. UseSwaggerUI must come after UseSwagger.
        app.UseSwagger();
        app.UseSwaggerUI();

        app.UseHttpsRedirection();
        app.UseStaticFiles();

        app.UseRouting();

        // FIX: UseCors must be placed between UseRouting and UseAuthentication/UseAuthorization.
        app.UseCors("AllowAll");

        app.UseAuthentication();
        app.UseAuthorization();

        app.MapControllers();
        app.Run();
    }

}
