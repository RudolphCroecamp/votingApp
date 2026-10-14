using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Http.HttpResults;
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



        public async Task<string> Login(login_requestDto creds) {
            try
            {
                // Find the account associated with the supplied ID number.
                var user = await _votingDbContext.auth.FirstOrDefaultAsync(a => a.id_number == creds.id_number);

                // Stop if no account matches the supplied ID.
                if(user == null) throw new UnauthorizedAccessException("User not found");


                //check if user is allowed access to the services
                if(user.allowed == false) throw new UnauthorizedAccessException("User does not have access to the service");

                //block admin request
                if(user.role == "Admin") throw new UnauthorizedAccessException("User does not have access to these services");

                // Stop if the account is inactive
                if (!user.is_active) throw new Exception("Account is inactive");

                // Embeddings must have equal lengths to be compared.
                if (user.face_embedding.Length != creds.face_embedding.Length) throw new UnauthorizedAccessException("Invalid face embedding");

                // Compare the submitted face embedding with the saved embedding.
                double similarity = CosineSimilarity(user.face_embedding, creds.face_embedding);

                // Reject embeddings that do not meet the configured similarity threshold.
                if (similarity < SIMULARITYSCORE) throw new UnauthorizedAccessException($"Face embedding does not match, similarity score: {similarity}"); ;

                // Issue a signed token for the authenticated account.
                string token = await GenerateToken(user.voter_id, user.role);

                //TODO: update login status in db

                return token;


            }
            catch(Exception ex) {
                throw new Exception(ex.Message);
            }
        
        
        }

        public async Task<string> AdminLogin(AdminLoginDTO creds)
        {
            try
            {
                // Find the account associated with the supplied ID number.
                var user = await _votingDbContext.auth.FirstOrDefaultAsync(a => a.id_number == creds.id_number);

                // Stop if no account matches the supplied ID.
                if (user == null) throw new UnauthorizedAccessException("User not found");


                //check if user is allowed access to the services
                if (user.allowed == false) throw new UnauthorizedAccessException("User does not have access to the service");

                //block voter request
                if (user.role == "Voter") throw new UnauthorizedAccessException("User does not have access to these services");

                //check password
                var passwordHasher = new PasswordHasher<object>();

                var result = passwordHasher.VerifyHashedPassword(null!, user.password, creds.password);

                if (result == PasswordVerificationResult.Failed) throw new UnauthorizedAccessException("Invalid ID number or password.");

                // Issue a signed token for the authenticated account.
                string token = await GenerateToken(user.voter_id, user.role);

                //TODO: update login status in db

                return token;

            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message);
            }


        }








        public async Task<string> GenerateToken(Guid voter_id, string role)
        {
            try
            {
                var issuedAt = DateTime.UtcNow;
                var existingSession = await _votingDbContext.auth_sessions
                    .FirstOrDefaultAsync(session =>
                        session.voter_id == voter_id &&
                        session.expires_at > issuedAt &&
                        session.revoked_at == null);

                var isNewSession = existingSession == null;
                var session = existingSession ?? new AuthSession
                {
                    jti = Guid.NewGuid(),
                    voter_id = voter_id,
                    created_at = issuedAt,
                    expires_at = issuedAt.AddMinutes(double.Parse(_configuration["ExpirationMinutes"]!))
                };

                // Create the token payload with the user identity, assigned role, and a unique token ID.
                var claims = new[]
                {
                    new Claim(ClaimTypes.NameIdentifier, voter_id.ToString()),
                    new Claim(ClaimTypes.Role, role),
                    new Claim(JwtRegisteredClaimNames.Jti, session.jti.ToString()),
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
                    expires: session.expires_at,
                    signingCredentials: credentials
                );

                if (isNewSession)
                {
                    await _votingDbContext.auth_sessions.AddAsync(session);
                    await _votingDbContext.SaveChangesAsync();
                }

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
                    role = "Voter",
                    face_embedding = request.face_embedding,
                    allowed = true,//NOTE voters should probably be checked out before allowing them
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
            var tokenId = principal?.FindFirst(JwtRegisteredClaimNames.Jti)?.Value;

            if (!Guid.TryParse(userId, out var voterId) || !Guid.TryParse(tokenId, out var jti)) return false;

            // A correctly signed JWT is accepted only while its matching DB session remains active.
            return await ValidateSession(voterId, jti);
        }

        public async Task<bool> ValidateSession(Guid voterId, Guid jti)
        {
            var now = DateTime.UtcNow;

            // Inactive accounts invalidate all their sessions, regardless of session state.
            var accountIsActive = await _votingDbContext.auth
                .AnyAsync(user => user.voter_id == voterId && user.is_active);

            if (!accountIsActive) return false;

            // Require an exact user/JTI match and reject expired or revoked sessions.
            return await _votingDbContext.auth_sessions.AnyAsync(session =>
                session.jti == jti &&
                session.voter_id == voterId &&
                session.revoked_at == null &&
                session.expires_at > now);
        }

        public async Task<string?> RefreshToken(string token)
        {
            // Accept only a valid token containing an account ID and unique token ID.
            var principal = GetValidatedPrincipal(token);
            var userId = principal?.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            var tokenId = principal?.FindFirst(JwtRegisteredClaimNames.Jti)?.Value;

            if (!Guid.TryParse(userId, out var voterId) || !Guid.TryParse(tokenId, out var jti)) return null;

            // Load the active account so its current role is used in the replacement token.
            var user = await _votingDbContext.auth.FirstOrDefaultAsync(account => account.voter_id == voterId && account.allowed);

            if (user == null) return null;

            // Consume the old session and issue its replacement to prevent token reuse.
            var now = DateTime.UtcNow;

            var session = await _votingDbContext.auth_sessions
                    .FirstOrDefaultAsync(session => 
                    session.jti == jti
                    && session.voter_id == voterId
                    && session.revoked_at == null
                    && session.expires_at > now);

                // If session is not found, revoked or expired, return null to indicate that the token cannot be refreshed.
                if (session == null) return null;

                // Revoke old session to prevent reuse
                session.revoked_at = now;
                await _votingDbContext.SaveChangesAsync();

            // Replace the consumed session with a fresh token/session in the same transaction.
            var refreshedToken = await GenerateToken(user.voter_id, user.role);

            return refreshedToken;
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
                    ClockSkew = TimeSpan.Zero
                };

                // Validate the token and require its account ID and unique token ID claims.
                var principal = tokenHandler.ValidateToken(token, validationParameters, out _);
                var userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                var tokenId = principal.FindFirst(JwtRegisteredClaimNames.Jti)?.Value;

                if (!Guid.TryParse(userId, out _) || !Guid.TryParse(tokenId, out _)) return null;

                return principal;
            }
            catch
            {
                return null;
            }
        }

        public async Task<bool> Logout(string token)
        {
            try
            {
                // Validate the submitted token and read its account ID.
                if (string.IsNullOrWhiteSpace(token)) throw new Exception("Token is required");

                var principal = GetValidatedPrincipal(token);
                var tokenVoterId = principal?.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                var tokenId = principal?.FindFirst(JwtRegisteredClaimNames.Jti)?.Value;
                if (!Guid.TryParse(tokenVoterId, out var voterId) || !Guid.TryParse(tokenId, out var jti))
                    throw new Exception("Token is invalid");

                var session = await _votingDbContext.auth_sessions
                    .FirstOrDefaultAsync(item => item.jti == jti && item.voter_id == voterId);

                if (session == null) return false;

                // Persist revocation so subsequent validation rejects this JWT before its expiry.
                if (session.revoked_at == null)
                {
                    session.revoked_at = DateTime.UtcNow;
                    await _votingDbContext.SaveChangesAsync();
                }

                return true;
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message);
            }
        }

    }
}
