using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.RateLimiting;
using votingBackend3.Models.DTOs;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using votingBackend3.Services;

namespace votingBackend3.Controllers
{
    //[EnableRateLimiting("Authentication")]
    [Route("api/[controller]")]
    [ApiController]
    public class AuthController : ControllerBase
    {
        private readonly AuthService _authService;

        public AuthController(AuthService authService)
        {
            _authService = authService;
        }



        //use users credentials to log them in
        [HttpPost]
        [Route("login")]
        public async Task<IActionResult> Login([FromBody] login_requestDto request) {
            try
            {
                var token = await _authService.Login(request);

                if (token == null || token.ToString().Length <= 0) throw new Exception("Something went wrong try again later");

                return Ok(new
                {
                    message = "Login success",
                    token = token
                });

            }
            catch (UnauthorizedAccessException ex)
            {
                Console.WriteLine(ex.Message);

                return BadRequest(new
                {
                    message = $"Login Error: {ex.Message}",
                });

            }
            catch (Exception ex) {
                Console.WriteLine(ex.Message);

                return BadRequest(new {
                    message = $"Login Error: {ex.Message}",
                });
            
            }
            

        }


        [HttpPost]
        [Route("AdminLogin")]
        public async Task<IActionResult> AdminLogin([FromBody] AdminLoginDTO creds)
        {

            try
            {
                var token = await _authService.AdminLogin(creds);

                if (token == null || token.ToString().Length <= 0) throw new Exception("Something went wrong try again later");

                return Ok(new
                {
                    message = "Login success",
                    token = token
                });

            }
            catch (UnauthorizedAccessException ex) {
                return Unauthorized(new
                {
                    message = $"Login Error: {ex.Message}",
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);

                return BadRequest(new
                {
                    message = $"Login Error: {ex.Message}",
                });

            }


        }



        [HttpPost]
        [Route("register")]
        [Consumes("multipart/form-data")]
        public async Task<IActionResult> Register([FromForm] register_request request)
        {
            try
            {
                var success = await _authService.Register(request);

                if (!success)
                {
                    return BadRequest(new
                    {
                        message = "Registration failed"
                    });
                }

                return Ok(new
                {
                    message = "Registration successful"
                });
            }
            catch (ArgumentException ex)
            {
                return BadRequest(new
                {
                    message = ex.Message
                });
            }
            catch (InvalidOperationException ex)
            {
                return Conflict(new
                {
                    message = ex.Message
                });
            }
            catch (Exception ex)
            {
                // Log the full exception on the server.
                Console.WriteLine(ex);

                return StatusCode(500, new
                {
                    message = "An unexpected error occurred during registration."
                });
            }
        }




        [HttpPost]
        [Route("validate")]
        public async Task<IActionResult> ValidateToken([FromBody] token_request request)
        {
            try
            {
                var isValid = await _authService.ValidateToken(request.token);

                if (!isValid)
                {
                    return Unauthorized(new
                    {
                        message = "Token is invalid or expired",
                    });
                }

                return Ok(new
                {
                    message = "Token valid",
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);

                return BadRequest(new
                {
                    message = $"Validation Error: {ex.Message}",
                });
            }
        }

        [HttpPost]
        [Route("refresh")]
        public async Task<IActionResult> RefreshToken([FromBody] token_request request)
        {
            try
            {
                var token = await _authService.RefreshToken(request.token);

                if (string.IsNullOrWhiteSpace(token))
                {
                    return Unauthorized(new
                    {
                        message = "Token is invalid, expired, or its account is inactive",
                    });
                }

                return Ok(new
                {
                    message = "Token refreshed",
                    token = token
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);

                return BadRequest(new
                {
                    message = $"Token refresh error: {ex.Message}",
                });
            }
        }

        [Authorize(Roles = "Voter")]
        [HttpGet]
        [Route("me")]
        public async Task<IActionResult> Me()
        {
            var voterIdValue = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            var jtiValue = User.FindFirst(JwtRegisteredClaimNames.Jti)?.Value;

            if (!Guid.TryParse(voterIdValue, out var voterId) || !Guid.TryParse(jtiValue, out var jti))
                return Unauthorized(new { message = "Token is missing a valid voter ID or JTI." });

            if (!await _authService.ValidateSession(voterId, jti))
                return Unauthorized(new { message = "Token session is invalid, expired, or revoked." });

            var profile = await _authService.GetProfile(voterId);
            if (profile == null) return NotFound(new { message = "Voter not found" });

            return Ok(new
            {
                message = "Voter profile",
                profile = profile
            });
        }

        [HttpPost]
        [Route("logout")]
        public async Task<IActionResult> Logout([FromBody] logout_request request)
        {
            try
            {
                var success = await _authService.Logout(request.token);

                if (!success)
                {
                    return BadRequest(new
                    {
                        message = "Logout failed",
                    });
                }

                // The client must delete its locally stored token after receiving this confirmation.
                return Ok(new
                {
                    message = "Logout success",
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);

                return BadRequest(new
                {
                    message = $"Logout Error: {ex.Message}",
                });
            }
        }

    }
}
