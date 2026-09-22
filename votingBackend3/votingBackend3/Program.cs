using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Scalar.AspNetCore;
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
var jwtKey = builder.Configuration["Jwt:Key"];

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
            ValidIssuer = builder.Configuration["Jwt:Issuer"],

            ValidateAudience = true,
            ValidAudience = builder.Configuration["Jwt:Audience"],

            ValidateLifetime = true,

            ClockSkew = TimeSpan.Zero
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