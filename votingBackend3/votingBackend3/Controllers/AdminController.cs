using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using Microsoft.EntityFrameworkCore;
using System.Net.NetworkInformation;
using votingBackend3.Models.DTOs;
using votingBackend3.Models.Entities;
using votingBackend3.Services;
using static Microsoft.EntityFrameworkCore.DbLoggerCategory;


namespace votingBackend3.Controllers
{
    [Authorize(Roles = "Admin")]
    [Route("api/[controller]")]
    [ApiController]
    public class AdminController : ControllerBase
    {

        private readonly AdminService _adminService;
        private readonly BlockchainService _blockchainService;
        private readonly IConfiguration _configuration;
        private readonly int NODECOUNT = 0;

        public AdminController(AdminService adminService, BlockchainService blockchainService, IConfiguration configuration)
        {
            _adminService = adminService;
            _blockchainService = blockchainService;
            _configuration = configuration;
            NODECOUNT = _configuration.GetValue<int>("NODECOUNT"!);
        }


        [HttpGet]
        [Route("get_elections")]
        public async Task<IActionResult> GetElections()
        {
            try
            {
                List<Election> elecions = await _adminService.GetElections();

                return Ok(new
                {
                    message = "Elections Information",
                    elections = elecions
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                return BadRequest(new
                {
                    message = $"Error: {ex.Message}"
                });
            }

        }


        [HttpGet]
        [Route("get_parties")]
        public async Task<IActionResult> GetParties()
        {
            try
            {
                List<Party> parties = await _adminService.GetParties();

                return Ok(new
                {
                    message = "Parties Information",
                    parties = parties
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                return BadRequest(new
                {
                    message = $"Error: {ex.Message}"
                });
            }

        }



        [HttpPost]
        [Route("new_election")]
        public async Task<IActionResult> CreateElection([FromBody] new_electionDto request)
        {
            try
            {
                Election new_election = await _adminService.CreateElection(request);

                if (new_election == null)
                {
                    throw new Exception("Could not add new Election");
                }

                return Ok(new
                {
                    message = "Election created successfully",
                    new_election = new_election
                });

            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                return BadRequest(new
                {
                    message = $"Error: {ex.Message}"
                });

            }

        }


        [HttpPost]
        [Route("new_party")]
        public async Task<IActionResult> CreateParty([FromBody] new_partyDto request)
        {
            try
            {
                Party new_party = await _adminService.CreateParty(request);

                if (new_party == null)
                {
                    throw new Exception("Could not add new Party");
                }

                return Ok(new {
                    message = "Party created successfully",
                    party = new_party

                });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                return BadRequest(new
                {
                    message = $"Error: {ex.Message}"
                });
            }

        }




        [HttpPatch]
        [Route("update_election_status")]
        public async Task<IActionResult> updateElectionStatus([FromBody] updateStatusDto request)
        {
            try
            {
                Election election = await _adminService.updateElectionStatus(request);

                if (election == null) throw new Exception("Failed to update election status.");

                return Ok(new
                {
                    message = "Successfully updated election status.",
                    election = election
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                return BadRequest(new
                {
                    message = $"Error: {ex.Message}"
                });
            }


        }


        [HttpPatch]
        [Route("update_party_status")]
        public async Task<IActionResult> updatePartyStatus([FromBody] updateStatusDto request)
        {
            try
            {
                Party party = await _adminService.updatePartyStatus(request);

                if (party == null) throw new Exception("Failed to update party status.");

                return Ok(new
                {
                    message = "Successfully updated party status.",
                    party = party
                });

            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                return BadRequest(new
                {
                    message = $"Error: {ex.Message}"
                });
            }

        }



        [HttpPut]
        [Route("updateElection{election_id}")]
        public async Task<IActionResult> updateElection(Guid election_id, updateElectionDto request)
        {
            var updatedElection = await _adminService.updateElectionInformation(election_id, request);

            if (updatedElection == null)
            {
                return NotFound(new
                {
                    message = "Election not found or update failed."
                });
            }

            return Ok(new
            {
                message = "Election updated successfully.",
                election = updatedElection
            });


        }


        [HttpPut]
        [Route("updateParty{party_id}")]
        public async Task<IActionResult> updateParty(Guid party_id, updatePartyDto request)
        {
            var updatedParty = await _adminService.updatePartyInformation(party_id, request);
            if (updatedParty == null)
            {
                return NotFound(new
                {
                    message = "Party not found or update failed."
                });
            }
            return Ok(new
            {
                message = "Party updated successfully.",
                party = updatedParty
            });


        }




        //FIX blockchain sould handle multiple elections and parties, and should be able to return the results of a specific election
        //check each node's blockchain and see if they are valid
        [HttpGet]
        [Route("isBlockchainValid")]
        public async Task<IActionResult> isBlockchainValid()
        {
            try
            {
                var blockchain_result = await _blockchainService.IsBlockchainValid(NODECOUNT);

                if (blockchain_result == null) return NotFound("No blockchain data found");

                return Ok(new
                {
                    message = "Blockchain data",
                    blockchain = blockchain_result
                });

            }
            catch (Exception ex) {
                return BadRequest(new
                {
                    message = $"Error: {ex.Message}"
                });
            }
            

        }



        [HttpGet]
        [Route("GetElectionResults/{election_id}")]
        public async Task<IActionResult> GetElectionResults([FromRoute] Guid election_id)
        {
            try
            {
                var results = await _adminService.GetElectionResults(election_id);
                return Ok(new
                {
                    message = "Election Results",
                    results = results
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                return BadRequest(new
                {
                    message = $"Error: {ex.Message}"
                });
            }
        }


        //FIX could have more than one winner, should return a list of winners
        [HttpGet]
        [Route("GetElectionWinner/{election_id}")]
        public async Task<IActionResult> GetElectionWinner([FromRoute] Guid election_id)
        {
            try
            {
                var winner = await _adminService.GetElectionWinner(election_id);

                return Ok(new
                {
                    message = "Election Winner",
                    election_id = election_id,
                    party_name = winner.party_name,
                    winnerVotes = winner.votes
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                return BadRequest(new
                {
                    message = $"Error: {ex.Message}"
                });
            }


        }



        [HttpGet]
        [Route("GetElectionVoters")]
        public async Task<IActionResult> GetElectionVoters([FromQuery] Guid election_id, [FromQuery] int limit = 10, [FromQuery] int skip = 0)
        {
            try
            {
                if (election_id == Guid.Empty)
                {
                    return BadRequest(new { message = "A valid election_id is required." });
                }

                if (limit < 1 || limit > 100)
                {
                    return BadRequest(new
                    {
                        message = "Limit must be between 1 and 100."
                    });
                }

                if (skip < 0)
                {
                    return BadRequest(new
                    {
                        message = "Skip cannot be negative."
                    });
                }

                var result = await _adminService.GetElectionVoters(
                    election_id,
                    limit,
                    skip
                );

                return Ok(new
                {
                    message = "Voters registered in this election retrieved successfully.",
                    election_voters = result
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);

                return StatusCode(500, new
                {
                    message = "An error occurred while retrieving election voters."
                });
            }
        }





        [HttpPost]
        [Route("assign_party_to_election")]
        public async Task<IActionResult> AssignPartyToElection([FromBody] AssignPartyToElectionDto request)
        {
            try
            {
                var assignment = await _adminService.AssignPartyToElection(request);

                return Ok(new
                {
                    message = "Party assigned to election successfully.",
                    assignment = new
                    {
                        party_id = assignment.party_id,
                        election_id = assignment.election_id
                    }
                });
            }
            catch (KeyNotFoundException ex)
            {
                return NotFound(new { message = ex.Message });
            }
            catch (InvalidOperationException ex)
            {
                return Conflict(new { message = ex.Message });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);

                return BadRequest(new
                {
                    message = "Failed to assign party to election."
                });
            }
        }


        [HttpGet]
        [Route("get_election_parties/{election_id:guid}")]
        public async Task<IActionResult> GetElectionParties(Guid election_id)
        {
            try
            {
                var parties = await _adminService.GetElectionParties(election_id);

                return Ok(new
                {
                    message = "Election parties retrieved successfully.",
                    parties = parties
                });
            }
            catch (KeyNotFoundException ex)
            {
                return NotFound(new { message = ex.Message });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);

                return BadRequest(new
                {
                    message = "Failed to retrieve election parties."
                });
            }
        }









        /// manage voters
        [HttpGet]
        [Route("GetAllVoters")]
        public async Task<IActionResult> GetAllVoters(int page = 1, int limit = 10)
        {
            var result = await _adminService.GetAllVoters(page, limit);

            return Ok(new
            {
                message = "Successfully received all the voters",
                voters = result
            });
        }

        [HttpGet]
        [Route("GetVoterByID/{voter_id:guid}")]
        public async Task<IActionResult> GetVoterByID(Guid voter_id)
        {
            var voter = await _adminService.GetVoterByID(voter_id);

            return Ok(new
            {
                message = "Successfully received voter details",
                voter = voter
            });
        }


        [HttpPatch]
        [Route("Update_voter_access")]
        public async Task<IActionResult> Update_voter_access(updateStatusDto request) {
            try
            {
                var user = await _adminService.Update_voter_access(request);

                if (user == null) throw new KeyNotFoundException("User not found");

                return Ok(new
                {
                    message = "Successfully updated user status",
                    user = user
                });
                
            }
            catch (Exception ex) {
                return BadRequest(new
                {
                    message = $"Failed to update user status: {ex.Message}"
                });
            }
            

        }

        //allow user to vote in the election
        [HttpPatch("AssignVoterToElection")]
        public async Task<IActionResult> AssignVoterToElection([FromBody] AssignElectionVoterDTO request)
        {
            try
            {
                var assignment = await _adminService.AssignVoterToElection(request);

                return Ok(new
                {
                    message = "Voter successfully assigned to election.",
                    election_id = assignment.election_id,
                    voter_id = assignment.voter_id,
                    has_voted = assignment.voted,
                    status = assignment.status
                });
            }
            catch (KeyNotFoundException ex)
            {
                return NotFound(new { message = ex.Message });
            }
            catch (InvalidOperationException ex)
            {
                return Conflict(new { message = ex.Message });
            }
        }




        ///Manage admins
        [HttpGet]
        [Route("GetAllAdmins")]
        public async Task<IActionResult> GetAllAdmins(int page = 1, int limit = 10)
        {
            var admins = await _adminService.GetAllAdmins();

            if (admins == null || admins.Count <= 0) return BadRequest(new
            {
                message = "No admins found"
            });

            return Ok(new
            {
                message = "Successfully retrieved admin details",
                admins = admins
            });

        }





        [HttpPost]
        [Route("NewAdmin")]
        public async Task<IActionResult> NewAdmin(newAdminDTO request) {
            try
            {
                var newAdminUser = await _adminService.NewAdmin(request);

                if (newAdminUser == null) return BadRequest(new
                {
                    message = "user not found"
                });

                return Ok(new
                {
                    message = "Successfully added new admin user",
                    user = newAdminUser
                });
            }
            catch (Exception ex) {
                return BadRequest(new
                {
                    message = ex.Message
                });
            }
        }


        [HttpPut]
        [Route("UpdateAdmin/{userId:guid}")]
        public async Task<IActionResult> UpdateAdmin(Guid userId, [FromBody] UpdateAdmin request)
        {
            try
            { 
                var result = await _adminService.UpdateAdmin(userId, request);

                if (result == null) return BadRequest(new
                {
                    message = "No user data found"
                });

                return Ok(new
                {
                    message = "Successfully updated admin information",
                    user = result
                });

            }
            catch (Exception ex)
            {
                return BadRequest(new
                {
                    message = ex.Message
                });
            }
        }

        




    }
}
