using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using votingBackend3.Data;
using votingBackend3.Models.DTOs;

namespace votingBackend3.Services
{
    public class AuthService
    {
        private readonly IConfiguration _configuration;
        private readonly VotingDbContext _votingDbContext;

        private readonly double SIMULARITYSCORE = 0.90;

        public AuthService(IConfiguration configuration, VotingDbContext votingDbContext)
        {
            _configuration = configuration;
            _votingDbContext = votingDbContext;
        }



        public async Task<string> Login(login_request creds) {
            try
            {
                //get user by id number
                var user = await _votingDbContext.auth.FirstOrDefaultAsync(a => a.id_number == creds.id_number);

                //check if user was found or not
                if(user == null) throw new Exception("User not found");

                //check if embedding length matches
                if (user.face_embedding.Length != creds.face_embedding.Length) throw new Exception("Invalid face embedding");

                //check if face embedding matches
                double similarity = CosineSimilarity(user.face_embedding, creds.face_embedding);

                if (similarity < SIMULARITYSCORE) throw new Exception($"Face embedding does not match, similarity score: {similarity}"); ;

                string token = await GenerateToken(user.voter_id, user.role);

                //TODO: update login status in db

                return token;


            }
            catch(Exception ex) {
                throw new Exception(ex.Message);
            }
        
        
        }












        public async Task<string> GenerateToken(Guid voter_id, string role)
        {
            try
            {
                var claims = new[]
                {
                    new Claim(ClaimTypes.NameIdentifier, voter_id.ToString()),
                    new Claim(ClaimTypes.Role, role),
                    new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString()),//add unique identifier to the token -> revoke/block it later
                };

                var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_configuration["Jwt:Key"]!));

                var credentials = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

                var token = new JwtSecurityToken(
                    issuer: _configuration["Jwt:Issuer"],
                    audience: _configuration["Jwt:Audience"],
                    claims: claims,
                    expires: DateTime.UtcNow.AddMinutes(double.Parse(_configuration["Jwt:ExpirationMinutes"]!)),
                    signingCredentials: credentials
                );

                return new JwtSecurityTokenHandler().WriteToken(token);
            }
            catch (Exception ex) {
                throw new Exception(ex.Message);
            }
            
        }



        private double CosineSimilarity(double[] a, double[] b)
        {
            if (a.Length != b.Length) throw new ArgumentException("Embedding sizes do not match.");

            double dotProduct = 0;
            double magnitudeA = 0;
            double magnitudeB = 0;

            for (int i = 0; i < a.Length; i++)
            {
                dotProduct += a[i] * b[i];
                magnitudeA += a[i] * a[i];
                magnitudeB += b[i] * b[i];
            }

            if (magnitudeA == 0 || magnitudeB == 0)
                return 0;

            return dotProduct / (Math.Sqrt(magnitudeA) * Math.Sqrt(magnitudeB));
        }


    }
}
