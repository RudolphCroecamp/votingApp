using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.RateLimiting;
using Microsoft.EntityFrameworkCore;
using votingBackend3.Data;
using votingBackend3.Models.DTOs;

namespace votingBackend3.Controllers
{
    // Read-only election schedule for the voter app (home screen countdown and reminders).
    [EnableRateLimiting("GlobalApi")]
    [Authorize(Roles = "Voter")]
    [Route("api/[controller]")]
    [ApiController]
    public class ElectionsController : ControllerBase
    {
        private readonly VotingDbContext _votingDbContext;

        public ElectionsController(VotingDbContext votingDbContext)
        {
            _votingDbContext = votingDbContext;
        }

        //elections that have not ended yet, soonest first
        [HttpGet]
        [Route("upcoming")]
        public async Task<IActionResult> GetUpcomingElections()
        {
            var today = DateOnly.FromDateTime(DateTime.UtcNow);

            var elections = await _votingDbContext.election
                .AsNoTracking()
                .Where(e => e.end_date >= today)
                .OrderBy(e => e.start_date)
                .ThenBy(e => e.election_id)
                .Select(e => new ElectionDto
                {
                    election_id = e.election_id,
                    election_name = e.election_name,
                    election_type = e.election_type,
                    start_date = e.start_date,
                    end_date = e.end_date,
                    isActive = e.isActive
                })
                .ToListAsync();

            return Ok(new
            {
                message = "Upcoming elections",
                elections = elections
            });
        }
    }
}
