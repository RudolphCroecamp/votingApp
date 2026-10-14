using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.RateLimiting;
using System.Diagnostics;
using System.Net.Http.Json;
using System.Text.Json;
using votingBackend3.Data;





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
        private readonly VotingDbContext _votingDbContext;

        public HealthController(IWebHostEnvironment environment, VotingDbContext votingDbContext)
        {
            _environment = environment;
            _votingDbContext = votingDbContext;
        }

        [HttpGet]
        [Route("health")]
        public async Task<IActionResult> GetHealth()
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



        [HttpGet]
        [Route("db_health")]
        public async Task<IActionResult> Db_health() {
            try
            {
                bool connected = await _votingDbContext.Database.CanConnectAsync();

                if (connected)
                {
                    return Ok(new
                    {
                        status = "Healthy",
                        database = "PostgreSQL"
                    });
                }

                return BadRequest(new {
                    status = "Unhealthy",
                    database = "PostgreSQL"
                });
            }
            catch
            {
                return BadRequest(new
                {
                    status = "Unhealthy",
                    database = "PostgreSQL"
                });
            }
        }
    


        [HttpGet]
        [Route("nodes")]
        public async Task<IActionResult> GetNodeHealth([FromServices] IHttpClientFactory httpClientFactory, CancellationToken cancellationToken)
        {
            var client = httpClientFactory.CreateClient();
            client.Timeout = TimeSpan.FromSeconds(3);

            var nodes = new Dictionary<string, string>
            {
                ["node1"] = "http://node1:8080/api/Health/health",
                ["node2"] = "http://node2:8080/api/Health/health",
                ["node3"] = "http://node3:8080/api/Health/health",
                ["node4"] = "http://node4:8080/api/Health/health"
            };

            var tasks = nodes.Select(async node =>
            {
                var stopwatch = Stopwatch.StartNew();

                try
                {
                    using var response = await client.GetAsync(
                        node.Value,
                        cancellationToken);

                    stopwatch.Stop();

                    var data = response.IsSuccessStatusCode
                        ? await response.Content.ReadFromJsonAsync<JsonElement>(
                            cancellationToken: cancellationToken)
                        : default;

                    var healthy =
                        response.IsSuccessStatusCode &&
                        data.ValueKind == JsonValueKind.Object &&
                        data.TryGetProperty("status", out var status) &&
                        status.GetString()?.Equals(
                            "Healthy",
                            StringComparison.OrdinalIgnoreCase) == true;

                    return new
                    {
                        node = node.Key,
                        status = healthy ? "Healthy" : "Unhealthy",
                        latencyMs = stopwatch.ElapsedMilliseconds,
                        uptimeSeconds = healthy &&
                            data.TryGetProperty("uptimeSeconds", out var uptime)
                                ? uptime.GetInt64()
                                : (long?)null,
                        timestamp = healthy &&
                            data.TryGetProperty("timestamp", out var timestamp)
                                ? timestamp.GetString()
                                : null
                    };
                }
                catch (Exception ex) when (
                    ex is HttpRequestException ||
                    ex is OperationCanceledException ||
                    ex is JsonException ||
                    ex is InvalidOperationException ||
                    ex is FormatException)
                {
                    stopwatch.Stop();

                    return new
                    {
                        node = node.Key,
                        status = "Unreachable",
                        latencyMs = stopwatch.ElapsedMilliseconds,
                        uptimeSeconds = (long?)null,
                        timestamp = (string?)null
                    };
                }
            });

            var results = await Task.WhenAll(tasks);
            var allHealthy = results.All(n => n.status == "Healthy");

            return StatusCode(allHealthy ? 200 : 503, new
            {
                status = allHealthy ? "Healthy" : "Degraded",
                timestamp = DateTime.UtcNow,
                totalNodes = results.Length,
                healthyNodes = results.Count(n => n.status == "Healthy"),
                nodes = results
            });
        } 
    }
}
