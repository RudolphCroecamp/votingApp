using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.RateLimiting;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Options;
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
builder.Services.AddHttpClient();
builder.Services.AddScoped<AdminService>();
builder.Services.AddScoped<AuthService>();
builder.Services.AddScoped<VoteService>();
builder.Services.AddScoped<DocumentService>();
builder.Services.AddHttpClient();
builder.Services.AddScoped<BlockchainService>(provider =>
{
    var blocksDbContext =
        provider.GetRequiredService<BlocksDbContext>();

    var httpClientFactory =
        provider.GetRequiredService<IHttpClientFactory>();

    var httpClient = httpClientFactory.CreateClient();

    var nodeTokenService =
        provider.GetRequiredService<NodeTokenService>();

    return new BlockchainService(
        blocksDbContext,
        httpClient,
        nodeTokenService
     );
});

builder.Services.AddSingleton<NodeTokenService>();


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
var jwtKey = builder.Configuration["JWTKey"]; //?? throw new InvalidOperationException("JWTKey is missing.");
var nodeSigningKey = builder.Configuration["NodeAuth:SigningKey"]; //?? throw new InvalidOperationException("NodeAuth:SigningKey is missing.");
var nodeIssuer = builder.Configuration["NodeAuth:Issuer"]; //?? throw new InvalidOperationException( "NodeAuth:Issuer is missing.");
var nodeAudience = builder.Configuration["NodeAuth:Audience"]; //?? throw new InvalidOperationException("NodeAuth:Audience is missing.");

builder.Services.AddAuthentication(
    JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        // Existing voter/admin JWT authentication
        options.TokenValidationParameters =
            new TokenValidationParameters
            {
                ValidateIssuerSigningKey = true,
                IssuerSigningKey = new SymmetricSecurityKey(
                    Encoding.UTF8.GetBytes(jwtKey)),

                ValidateIssuer = true,
                ValidIssuer = builder.Configuration["Issuer"],

                ValidateAudience = true,
                ValidAudience = builder.Configuration["Audience"],

                ValidateLifetime = true,
                ClockSkew = TimeSpan.Zero
            };
    })
    .AddJwtBearer("NodeBearer", options =>
    {
        // Separate service-to-service JWT authentication
        options.TokenValidationParameters =
            new TokenValidationParameters
            {
                ValidateIssuerSigningKey = true,
                IssuerSigningKey = new SymmetricSecurityKey(
                    Convert.FromBase64String(nodeSigningKey)),

                ValidateIssuer = true,
                ValidIssuer = nodeIssuer,

                ValidateAudience = true,
                ValidAudience = nodeAudience,

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

builder.Services.AddAuthorization(options =>
{
    options.AddPolicy("NodeVoteReceiver", policy =>
    {
        policy.AddAuthenticationSchemes("NodeBearer");
        policy.RequireAuthenticatedUser();
        policy.RequireClaim("service", "voting-backend");
        policy.RequireClaim("permission", "votes.broadcast");
    });

});





//setup rate limiting to the services

builder.Services.AddRateLimiter(options =>
{
    options.RejectionStatusCode = StatusCodes.Status429TooManyRequests;

    //General API protection
    options.AddFixedWindowLimiter("GlobalApi", limiter =>
    {
        limiter.PermitLimit = 120;
        limiter.Window = TimeSpan.FromMinutes(1);
        limiter.QueueLimit = 0;
        limiter.AutoReplenishment = true;
    });

    //Login and face-verification protection
    options.AddFixedWindowLimiter("Authentication", limiter =>
    {
        limiter.PermitLimit = 5;
        limiter.Window = TimeSpan.FromMinutes(1);
        limiter.QueueLimit = 0;
        limiter.AutoReplenishment = true;
    });

    //Vote-submission protection
    options.AddFixedWindowLimiter("VoteSubmission", limiter =>
    {
        limiter.PermitLimit = 10;
        limiter.Window = TimeSpan.FromMinutes(1);
        limiter.QueueLimit = 0;
        limiter.AutoReplenishment = true;
    });

    options.OnRejected = async (context, cancellationToken) =>
    {
        context.HttpContext.Response.StatusCode =
            StatusCodes.Status429TooManyRequests;

        context.HttpContext.Response.Headers["Retry-After"] = "60";

        await context.HttpContext.Response.WriteAsJsonAsync(
            new
            {
                message = "Too many requests. Please try again shortly."
            },
            cancellationToken);
    };

});

//apply CORS
builder.Services.AddCors(options =>
{
    options.AddPolicy("ReactFrontend", policy =>
    {
        policy
            .WithOrigins("http://localhost:5173")
            .AllowAnyHeader()
            .AllowAnyMethod();
    });
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


app.UseRouting();
app.UseCors("ReactFrontend");//added cors so frontend can access backend

app.UseRateLimiter();


app.UseHttpsRedirection();

app.UseAuthentication();
app.UseAuthorization();

app.MapControllers();

app.Run();






//FIX register we need to add the user's facial image
//FIX do we need voter street address
//FIX should we automatically allow all users or should they request access and an admin needs to allow them to participate in the election
//FIX admin users should not be added to the election_voter table
//FIX check if user has status = 'approved' before they can cast a vote
//example
/*
 var eligible = await _votingDbContext.ElectionVoter
    .AnyAsync(ev =>
        ev.election_id == electionId &&
        ev.voter_id == voterId &&
        ev.status == ElectionVoterStatus.Approved &&
        !ev.has_voted);

if (!eligible)
{
    throw new UnauthorizedAccessException(
        "You are not eligible to vote in this election.");
}
 
 */