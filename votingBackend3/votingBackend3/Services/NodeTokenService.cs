using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;

namespace votingBackend3.Services
{
    public class NodeTokenService
    {
        private readonly IConfiguration _configuration;

        public NodeTokenService(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        public string GenerateToken()
        {
            var key = _configuration["NodeAuth:SigningKey"]?? throw new InvalidOperationException("Node signing key is missing.");
            var issuer = _configuration["NodeAuth:Issuer"]!;
            var audience = _configuration["NodeAuth:Audience"]!;

            var credentials = new SigningCredentials(new SymmetricSecurityKey(Convert.FromBase64String(key)), SecurityAlgorithms.HmacSha256);

            var claims = new[]
            {
                new Claim(JwtRegisteredClaimNames.Sub, "voting-backend"),
                new Claim("service", "voting-backend"),
                new Claim("permission", "votes.broadcast")
            };

            var token = new JwtSecurityToken(
                issuer: issuer,
                audience: audience,
                claims: claims,
                notBefore: DateTime.UtcNow,
                expires: DateTime.UtcNow.AddMinutes(2),
                signingCredentials: credentials);

            return new JwtSecurityTokenHandler().WriteToken(token);
        }
    }
}
