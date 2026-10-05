using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using votingBackend3.Models.DTOs;
using votingBackend3.Services;

namespace votingBackend3.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class AuthController : ControllerBase
    {
        private readonly AuthService _authService;

        public AuthController(AuthService authService)
        {
            _authService = authService;
        }




        [HttpPost]
        [Route("login")]
        public async Task<IActionResult> Login([FromBody] login_request request) {

            try
            {
                var token = await _authService.Login(request);

                if (token == null || token.ToString().Length <= 0)
                {

                    return Unauthorized(new
                    {
                        message = "Invalid credentials",
                        status = false
                    });
                }

                return Ok(new
                {
                    message = "Login success",
                    status = true,
                    token = token
                });

            }
            catch (Exception ex) {
                Console.WriteLine(ex.Message);

                return BadRequest(new {
                    message = $"Login Error: {ex.Message}",
                    status = false
                });
            
            }
            

        }

        [HttpPost]
        [Route("register")]
        public async Task<IActionResult> Register([FromBody] register_request request)
        {
            try
            {
                var success = await _authService.Register(request);

                if (!success)
                {
                    return BadRequest(new
                    {
                        message = "Registration failed",
                        status = false
                    });
                }

                return Ok(new
                {
                    message = "Registration success",
                    status = true
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);

                return BadRequest(new
                {
                    message = $"Registration Error: {ex.Message}",
                    status = false
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
                        status = false
                    });
                }

                return Ok(new
                {
                    message = "Token valid",
                    status = true
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);

                return BadRequest(new
                {
                    message = $"Validation Error: {ex.Message}",
                    status = false
                });
            }
        }

        [HttpPost]
        [Route("logout")]
        public async Task<IActionResult> Logout([FromBody] logout_request request)
        {
            try
            {
                var success = await _authService.Logout(request.voter_id, request.token);

                if (!success)
                {
                    return BadRequest(new
                    {
                        message = "Logout failed",
                        status = false
                    });
                }

                return Ok(new
                {
                    message = "Logout success",
                    status = true
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);

                return BadRequest(new
                {
                    message = $"Logout Error: {ex.Message}",
                    status = false
                });
            }
        }

    }
}
