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
                // Find the account associated with the supplied ID number.
                var user = await _votingDbContext.auth.FirstOrDefaultAsync(a => a.id_number == creds.id_number);

                // Stop if no account matches the supplied ID.
                if(user == null) throw new Exception("User not found");

                // Embeddings must have equal lengths to be compared.
                if (user.face_embedding.Length != creds.face_embedding.Length) throw new Exception("Invalid face embedding");

                // Compare the submitted face embedding with the saved embedding.
                double similarity = CosineSimilarity(user.face_embedding, creds.face_embedding);

                // Reject embeddings that do not meet the configured similarity threshold.
                if (similarity < SIMULARITYSCORE) throw new Exception($"Face embedding does not match, similarity score: {similarity}"); ;

                // Issue a signed token for the authenticated account.
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
                // Require authentication data and the voter profile fields needed by the database.
                if (string.IsNullOrWhiteSpace(request.id_number)) throw new Exception("Id number is required");
                if (string.IsNullOrWhiteSpace(request.role)) throw new Exception("Role is required");
                if (request.face_embedding == null || request.face_embedding.Length == 0) throw new Exception("Face embedding is required");
                if (string.IsNullOrWhiteSpace(request.firstname)) throw new Exception("First name is required");
                if (string.IsNullOrWhiteSpace(request.lastname)) throw new Exception("Last name is required");
                if (string.IsNullOrWhiteSpace(request.province)) throw new Exception("Province is required");
                if (string.IsNullOrWhiteSpace(request.city)) throw new Exception("City is required");
                if (request.postal <= 0) throw new Exception("Postal code is required");

                // Do not create a second account with the same ID number.
                var existingUser = await _votingDbContext.auth.FirstOrDefaultAsync(a => a.id_number == request.id_number);

                if (existingUser != null) throw new Exception("User already exists");

                // Use one ID to link the voter profile to its authentication record.
                var voterId = Guid.NewGuid();

                // Build the voter's profile from the submitted personal details.
                var newVoter = new Voter
                {
                    voter_id = voterId,
                    firstname = request.firstname,
                    lastname = request.lastname,
                    province = request.province,
                    city = request.city,
                    street = request.street,
                    postal = request.postal
                };

                // Build the active authentication record using the same voter ID.
                var newUser = new Auth
                {
                    voter_id = voterId,
                    id_number = request.id_number,
                    role = request.role,
                    face_embedding = request.face_embedding,
                    is_active = true,
                    created_at = DateTime.UtcNow
                };

                // Save both related records in the same database operation.
                await _votingDbContext.voter.AddAsync(newVoter);
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
            // Validate the JWT and read the account ID from its claims.
            var principal = GetValidatedPrincipal(token);
            var userId = principal?.FindFirst(ClaimTypes.NameIdentifier)?.Value;

            // Confirm that the token belongs to an account that is still active.
            if (!Guid.TryParse(userId, out var voterId)) return false;

            return await _votingDbContext.auth.AnyAsync(user => user.voter_id == voterId && user.is_active);
        }

        public async Task<string?> RefreshToken(string token)
        {
            // Accept only a valid token containing an account ID and unique token ID.
            var principal = GetValidatedPrincipal(token);
            var userId = principal?.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            var tokenId = principal?.FindFirst(JwtRegisteredClaimNames.Jti)?.Value;

            if (!Guid.TryParse(userId, out var voterId) || string.IsNullOrWhiteSpace(tokenId)) return null;

            // Load the active account so its current role is used in the replacement token.
            var user = await _votingDbContext.auth.FirstOrDefaultAsync(account => account.voter_id == voterId && account.is_active);

            if (user == null) return null;

            // Create a new token for the active account.
            return await GenerateToken(user.voter_id, user.role);
        }

        private ClaimsPrincipal? GetValidatedPrincipal(string token)
        {
            try
            {
                // Reject missing input before attempting JWT validation.
                if (string.IsNullOrWhiteSpace(token)) return null;

                // Prepare the JWT handler and signing key from application settings.
                var tokenHandler = new JwtSecurityTokenHandler();
                var key = Encoding.UTF8.GetBytes(_configuration["JWTKey"]!);

                // Require a valid signature, issuer, audience, and unexpired lifetime.
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

                // Validate the token and require its account ID and unique token ID claims.
                var principal = tokenHandler.ValidateToken(token, validationParameters, out _);
                var userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                var tokenId = principal.FindFirst(JwtRegisteredClaimNames.Jti)?.Value;

                if (!Guid.TryParse(userId, out _) || string.IsNullOrWhiteSpace(tokenId)) return null;

                return principal;
            }
            catch
            {
                return null;
            }
        }

        public async Task<bool> Logout(Guid voter_id, string token)
        {
            try
            {
                // Require an account ID and a currently valid token before logging out.
                if (voter_id == Guid.Empty) throw new Exception("Invalid voter id");
                if (string.IsNullOrWhiteSpace(token)) throw new Exception("Token is required");
                if (!await ValidateToken(token)) throw new Exception("Token is invalid or expired");

                // Disable the account to prevent it from being treated as active.
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
                // Find the account that should be disabled.
                var user = await _votingDbContext.auth.FirstOrDefaultAsync(a => a.voter_id == voter_id);

                if (user == null) return false;

                // Mark the account inactive and persist the change.
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
