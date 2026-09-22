using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using votingBackend3.Models.DTOs;
using votingBackend3.Models.Entities;
using votingBackend3.Services;

namespace votingBackend3.Controllers
{
    [Authorize(Roles = "admin")]
    [Route("api/[controller]")]
    [ApiController]
    public class AdminController : ControllerBase
    {

        private readonly AdminService _adminService;


        public AdminController(AdminService adminService)
        {
            _adminService = adminService;
        }


        [HttpGet]
        [Route("get_elections")]
        public async Task<IActionResult> GetElections()
        {
            List<Election> elecions = await _adminService.GetElections();

            return Ok(new { 
                message = "Elections Information",
                elections = elecions
            });
        }


        [HttpGet]
        [Route("get_parties")]
        public async Task<IActionResult> GetParties()
        {
            List<Party> parties = await _adminService.GetParties();

            return Ok(new
            {
                message = "Parties Information",
                elections = parties
            });
        }



        [HttpPost]
        [Route("new_election")]
        public async Task<IActionResult> CreateElection([FromBody] new_election request)
        {
            bool electionCreated = await _adminService.CreateElection(request);

            if (!electionCreated)
            {
                return BadRequest(new { message = "Could not add new Election" });
            }

            return Ok(new { message = "Election created successfully" } );
        }


        [HttpPost]
        [Route("new_party")]
        public async Task<IActionResult> CreateParty([FromBody] new_party request)
        {
            bool partyCreated = await _adminService.CreateParty(request);

            if (!partyCreated)
            {
                return BadRequest(new { message = "Could not add new Party" });
            }

            return Ok(new { message = "Party created successfully" });
        }




        [HttpPut]
        [Route("update_election_status")]
        public async Task<IActionResult> updateElectionStatus([FromBody] updateStatus request) {
            bool updated = await _adminService.updateElectionStatus(request);

            if (!updated) {
                return BadRequest(new {
                    message = "Failed to update election status.",
                    updated_status = request.status
                });
            }

            return Ok(new
            {
                message = "Successfully updated election status.",
                updated_status = request.status
            });
            
        }


        [HttpPut]
        [Route("update_party_status")]
        public async Task<IActionResult> updatePartyStatus([FromBody] updateStatus request)
        {
            bool updated = await _adminService.updatePartyStatus(request);

            if (!updated)
            {
                return BadRequest(new
                {
                    message = "Failed to update party status.",
                    updated_status = request.status
                });
            }

            return Ok(new
            {
                message = "Successfully updated party status.",
                updated_status = request.status
            });

        }

    }
}
