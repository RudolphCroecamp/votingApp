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

            var token = await _authService.Login(request);

            Console.WriteLine(token);

            if (token == null || token.ToString().Length <= 0) {

                return BadRequest(new {
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






    }
}
