using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.RateLimiting;
using System.Diagnostics;

namespace votingBackend3.Controllers
{
    [EnableRateLimiting("GlobalApi")]
    [Route("api/[controller]")]
    [ApiController]
    public class HealthController : ControllerBase
    {
        private static readonly DateTime StartTime = DateTime.UtcNow;

        private static readonly Stopwatch Uptime = Stopwatch.StartNew();

        private readonly IWebHostEnvironment _environment;

        public HealthController(IWebHostEnvironment environment)
        {
            _environment = environment;
        }

        [HttpGet]
        public IActionResult GetHealth()
        {
            return Ok(new
            {
                status = "Healthy",
                timestamp = DateTime.UtcNow,
                uptimeSeconds = (long)Uptime.Elapsed.TotalSeconds,
                environment = _environment.EnvironmentName,
                checks = new
                {
                    api = "Healthy"
                }
            });
        }
    }
}
