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
        var keyVaultUrl = "https://medic-helper-keyvault.vault.azure.net/"; 
        
        var secretClient = new SecretClient(new Uri(keyVaultUrl), new DefaultAzureCredential());

        var jwtKey = secretClient.GetSecret("JWTKey").Value.Value; 
        
        var jwtIssuer = secretClient.GetSecret("JWTIssuer").Value.Value; 
        
        var jwtAudience = secretClient.GetSecret("JWTAudience").Value.Value; 
        
        var firebaseAdminSdk = secretClient.GetSecret("FirebaseAdminSDK").Value.Value; 
        
        var connectionString = secretClient.GetSecret("ConnectionString").Value.Value;


        var builder = WebApplication.CreateBuilder(args);

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

        //ConfigureFirebase();

        using var secretStream = new MemoryStream(System.Text.Encoding.UTF8.GetBytes(firebaseAdminSdk));

        FirebaseApp.Create(new AppOptions
        {
            Credential = GoogleCredential.FromStream(secretStream)
        });

        builder.Services.AddAutoMapper(typeof(MappingProfile));
        builder.Services.AddControllers();
        builder.Services.AddScoped<IAuthService, AuthService>();
        builder.Services.AddScoped<IJwtTokenGenerator, JwtTokenGenerator>();
        builder.Services.AddScoped<NotificationService>();

        var app = builder.Build();

        if (app.Environment.IsDevelopment())
        {
            app.UseDeveloperExceptionPage();
        }
        else
        {
            app.UseExceptionHandler("/Home/Error");
            app.UseHsts();
        }

        app.UseHttpsRedirection();
        app.UseStaticFiles();

        app.UseRouting();

        app.UseAuthentication();
        app.UseAuthorization();

        app.MapControllers();
        app.Run();
    }

    public static void ConfigureFirebase()
    {
        string firebaseKey = Environment.GetEnvironmentVariable("FIREBASE_ADMIN_SDK");

        if (!string.IsNullOrEmpty(firebaseKey))
        {
            var path = Path.Combine(AppContext.BaseDirectory, "firebase_adminsdk.json");
            File.WriteAllText(path, firebaseKey); // Use synchronous File.WriteAllText instead

            FirebaseApp.Create(new AppOptions
            {
                Credential = GoogleCredential.FromFile(path)
            });
        }
        else
        {
            throw new Exception("Firebase Admin SDK key not found in environment variables.");
        }
    }


}
