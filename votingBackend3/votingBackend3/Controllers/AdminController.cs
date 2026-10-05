using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using votingBackend3.Models.DTOs;
using votingBackend3.Models.Entities;
using votingBackend3.Services;


namespace votingBackend3.Controllers
{
    //[Authorize(Roles = "Admin")]
    [Route("api/[controller]")]
    [ApiController]
    public class AdminController : ControllerBase
    {

        private readonly AdminService _adminService;
        private readonly BlockchainService _blockchainService;
        private readonly IConfiguration _configuration;
        private readonly int NODECOUNT = 0;
        private readonly ApplicationDbContext _context;



        public AdminController(AdminService adminService, BlockchainService blockchainService, IConfiguration configuration, ApplicationDbContext context)
        {
            _adminService = adminService;
            _blockchainService = blockchainService;
            _configuration = configuration;
            NODECOUNT = _configuration.GetValue<int>("NODECOUNT"!);
            _context = context;
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
        public async Task<IActionResult> CreateElection([FromBody] new_election request)
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
        public async Task<IActionResult> CreateParty([FromBody] new_party request)
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




        [HttpPut]
        [Route("update_election_status")]
        public async Task<IActionResult> updateElectionStatus([FromBody] updateStatus request)
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


        [HttpPut]
        [Route("update_party_status")]
        public async Task<IActionResult> updatePartyStatus([FromBody] updateStatus request)
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




        //check each node's blockchain and see if they are valid
        [HttpGet]
        [Route("isBlockchainValid")]
        public async Task<IActionResult> isBlockchainValid()
        {
            var blockchain = await _blockchainService.IsBlockchainValid(NODECOUNT);

            if (blockchain == null) return NotFound("No blockchain data found");

            return Ok(new
            {
                message = "Blockchain data",
                blockchain = blockchain
            });

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
        //Edit election
        public async Task<IActionResult> EditElection(int id)
        {
            var election = await _context.Elections.FindAsync(int id);
            if (election == null)
            {
                return NotFound()
            }
            return ViewResult(election);
        }
        //Post edited election
        [HttpPost]
        public async Task<IActionResult> EditElection(int id, new_election election)
        {
            if (id != election.ElectionId)
            {
                return NotFound();
            }
            if (ModelState.IsValid)
            {
                try
                {
                    _context.Update(election);
                    await _context.SaveChangesAsync();
                    TempData["Success"] = "Election updated successfully.";

                }
                catch (DbUpdateConcurrencyException)
                {
                    if(!_context.Elections.Any(e => e.ElectionId == id))
                    {
                        return NotFound();
                    }

                    throw;
                }
                //ElectionList is a placeholder
                return RedirectToAction("ElectionList");
            }
            return ViewResult(election);
        }
        //Edit election Party
        public async Task<IActionResult> EditParty(int id)
        {
            var party = await _context.Party.FindAsync(id);
            if (party == null)
            {
                return NotFound();
            }
            returnView(party);
        }
        [HttpPost]
        public async Task<IActionResult> EditParty(int id, new_party party)
        {
            //Check what the party ID fields name is
            if (id != party.PartyID)
            {
                return NotFound();
            }
            if (ModelState.IsValid) 
            {
                try
                {
                    _context.Update(party);
                    await _context.SaveChangesAsync();
                    TempData["Success"] = "Party updated successfully.";
                }
                catch (DbUpdateConcurrencyException) 
                {
                    if (!_context.Party.Any(p =>p.PartyId == id))
                    {
                        return NotFound();
                    }

                    throw;
                }
                return RedirectToAction("PartyList");
            }
            return View(party);
    }
}
