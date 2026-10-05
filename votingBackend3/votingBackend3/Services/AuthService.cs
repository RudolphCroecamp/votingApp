using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using votingBackend3.Data;
using votingBackend3.Models.DTOs;
using votingBackend3.Models.Entities;

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
                // Create the token payload with the user identity, assigned role, and a unique token ID.
                var claims = new[]
                {
                    new Claim(ClaimTypes.NameIdentifier, voter_id.ToString()),
                    new Claim(ClaimTypes.Role, role),
                    new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString()),// unique token ID used later for revocation or blocking
                };

                // Turn the JWT secret into a key that can be used to sign the token.
                var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_configuration["JWTKey"]!));

                // Create the signing credentials using the HMAC-SHA256 algorithm.
                var credentials = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

                // Build the JWT with issuer, audience, claims, expiration time, and signature.
                var token = new JwtSecurityToken(
                    issuer: _configuration["Issuer"],
                    audience: _configuration["Audience"],
                    claims: claims,
                    expires: DateTime.UtcNow.AddMinutes(double.Parse(_configuration["ExpirationMinutes"]!)),
                    signingCredentials: credentials
                );

                // Convert the JWT object into a serialized string that the client can send back.
                return new JwtSecurityTokenHandler().WriteToken(token);
            }
            catch (Exception ex) {
                throw new Exception(ex.Message);
            }
            
        }






        private double CosineSimilarity(double[] a, double[] b)
        {
            // Ensure both embeddings have the same length before comparing them.
            if (a.Length != b.Length) throw new ArgumentException("Embedding sizes do not match.");

            // Initialize totals used to calculate the cosine similarity score.
            double dotProduct = 0;
            double magnitudeA = 0;
            double magnitudeB = 0;

            // Multiply each matching value pair and accumulate the vector magnitudes.
            for (int i = 0; i < a.Length; i++)
            {
                dotProduct += a[i] * b[i];
                magnitudeA += a[i] * a[i];
                magnitudeB += b[i] * b[i];
            }

            // Avoid dividing by zero if either embedding is empty or zero-valued.
            if (magnitudeA == 0 || magnitudeB == 0)
                return 0;

            // Return the cosine similarity, which measures how aligned the two vectors are.
            return dotProduct / (Math.Sqrt(magnitudeA) * Math.Sqrt(magnitudeB));
        }






        public async Task<bool> Register(register_request request)
        {
            try
            {
                if (string.IsNullOrWhiteSpace(request.id_number)) throw new Exception("Id number is required");
                if (string.IsNullOrWhiteSpace(request.role)) throw new Exception("Role is required");
                if (request.face_embedding == null || request.face_embedding.Length == 0) throw new Exception("Face embedding is required");

                var existingUser = await _votingDbContext.auth.FirstOrDefaultAsync(a => a.id_number == request.id_number);

                if (existingUser != null) throw new Exception("User already exists");

                var newUser = new Auth
                {
                    voter_id = Guid.NewGuid(),
                    id_number = request.id_number,
                    role = request.role,
                    face_embedding = request.face_embedding,
                    is_active = true,
                    created_at = DateTime.UtcNow
                };

                await _votingDbContext.auth.AddAsync(newUser);
                await _votingDbContext.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message);
            }
        }

        public async Task<bool> ValidateToken(string token)
        {
            try
            {
                if (string.IsNullOrWhiteSpace(token)) return false;

                var tokenHandler = new JwtSecurityTokenHandler();
                var key = Encoding.UTF8.GetBytes(_configuration["JWTKey"]!);

                var validationParameters = new TokenValidationParameters
                {
                    ValidateIssuerSigningKey = true,
                    IssuerSigningKey = new SymmetricSecurityKey(key),
                    ValidateIssuer = true,
                    ValidIssuer = _configuration["Issuer"],
                    ValidateAudience = true,
                    ValidAudience = _configuration["Audience"],
                    ValidateLifetime = true,
                    ClockSkew = TimeSpan.FromMinutes(1)
                };

                var principal = tokenHandler.ValidateToken(token, validationParameters, out _);

                if (principal == null) return false;

                var userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                var jti = principal.FindFirst(JwtRegisteredClaimNames.Jti)?.Value;

                if (string.IsNullOrWhiteSpace(userId) || string.IsNullOrWhiteSpace(jti)) return false;

                return true;
            }
            catch
            {
                return false;
            }
        }

        public async Task<bool> Logout(Guid voter_id, string token)
        {
            try
            {
                if (voter_id == Guid.Empty) throw new Exception("Invalid voter id");
                if (string.IsNullOrWhiteSpace(token)) throw new Exception("Token is required");
                if (!await ValidateToken(token)) throw new Exception("Token is invalid or expired");

                return await RevokeToken(voter_id);
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message);
            }
        }

        public async Task<bool> RevokeToken(Guid voter_id)
        {
            try
            {
                var user = await _votingDbContext.auth.FirstOrDefaultAsync(a => a.voter_id == voter_id);

                if (user == null) return false;

                user.is_active = false;
                await _votingDbContext.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message);
            }
        }

        


    }
}
