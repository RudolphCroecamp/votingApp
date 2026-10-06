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



        //use users credentials to log them in
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






    }
}
