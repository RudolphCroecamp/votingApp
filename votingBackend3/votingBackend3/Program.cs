using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Scalar.AspNetCore;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using votingBackend3.Data;
using votingBackend3.Services;


var builder = WebApplication.CreateBuilder(args);

// Add services to the container.

builder.Services.AddControllers();
// Learn more about configuring OpenAPI at https://aka.ms/aspnet/openapi
builder.Services.AddOpenApi();

//add services
builder.Services.AddScoped<AdminService>();
builder.Services.AddScoped<AuthService>();
builder.Services.AddScoped<VoteService>();
builder.Services.AddScoped<DocumentService>();
builder.Services.AddHttpClient();
builder.Services.AddScoped<BlockchainService>(provider =>
{
    var blocksDbContext = provider.GetRequiredService<BlocksDbContext>();

    var httpClientFactory = provider.GetRequiredService<IHttpClientFactory>();
    var httpClient = httpClientFactory.CreateClient();

    return new BlockchainService(blocksDbContext, httpClient);
});


//add database
builder.Services.AddDbContext<VotingDbContext>(options =>
    options.UseNpgsql(
        builder.Configuration.GetConnectionString("MainDatabase")
    ));

builder.Services.AddDbContext<BlocksDbContext>(options =>
    options.UseNpgsql(
        builder.Configuration.GetConnectionString("BlockchainDatabase")
    ));





//configure auth settings
var jwtKey = builder.Configuration["JWTKey"];

builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuerSigningKey = true,
            IssuerSigningKey = new SymmetricSecurityKey(
                Encoding.UTF8.GetBytes(jwtKey!)
            ),

            ValidateIssuer = true,
            ValidIssuer = builder.Configuration["Issuer"],

            ValidateAudience = true,
            ValidAudience = builder.Configuration["Audience"],

            ValidateLifetime = true,

            ClockSkew = TimeSpan.Zero
        };

        // Add database-backed session checks after standard JWT signature and lifetime validation.
        options.Events = new JwtBearerEvents
        {
            OnTokenValidated = async context =>
            {
                // Extract the identity and session ID that must match the persisted session.
                var voterIdValue = context.Principal?.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                var jtiValue = context.Principal?.FindFirst(JwtRegisteredClaimNames.Jti)?.Value;

                if (!Guid.TryParse(voterIdValue, out var voterId) ||
                    !Guid.TryParse(jtiValue, out var jti))
                {
                    context.Fail("Token is missing a valid voter ID or JTI.");
                    return;
                }

                // Reject protected requests if the account/session is inactive, expired, or revoked.
                var authService = context.HttpContext.RequestServices.GetRequiredService<AuthService>();
                if (!await authService.ValidateSession(voterId, jti))
                    context.Fail("Token session is invalid, expired, or revoked.");
            }
        };
    });

var app = builder.Build();

var nodeRole = builder.Configuration["NODE_ROLE"];
if (nodeRole == "node")
{//create the blockchain node DB
    using var scope = app.Services.CreateScope();

    var blockchainDb = scope.ServiceProvider
        .GetRequiredService<BlocksDbContext>();

    blockchainDb.Database.Migrate();//create and add migrations to db

    // Initialize blockchain
    var blockchainService = scope.ServiceProvider
        .GetRequiredService<BlockchainService>();

    await blockchainService.AddgenesisBlock();
}
else {//create the mainDB
    using var scope = app.Services.CreateScope();

    var MainDb = scope.ServiceProvider
        .GetRequiredService<VotingDbContext>();

    MainDb.Database.Migrate();//create and add migrations to db
}



// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
    app.MapScalarApiReference(); //test api at http://localhost:5000/scalar/v1 or  http://localhost:8080/scalar/v1
}

app.UseHttpsRedirection();

app.UseAuthentication();
app.UseAuthorization();

app.MapControllers();

app.Run();