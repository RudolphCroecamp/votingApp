using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.RateLimiting;
using System.Security.Claims;
using System.Security.Cryptography.X509Certificates;
using votingBackend3.Models.DTOs;
using votingBackend3.Models.Entities;
using votingBackend3.Services;

namespace votingBackend3.Controllers
{
    [EnableRateLimiting("VoteSubmission")]
    [Authorize(Roles = "Voter")]
    [Route("api/[controller]")]
    [ApiController]
    public class VotesController : ControllerBase
    {
        private readonly VoteService _voteService;
        private readonly BlockchainService _blockchainService;
        private readonly IConfiguration _configuration;

        private readonly int NODECOUNT = 0;

        public VotesController(VoteService voteService, BlockchainService blockchainService, IConfiguration configuration)
        {
            _voteService = voteService;
            _blockchainService = blockchainService;
            _configuration = configuration;
            NODECOUNT = _configuration.GetValue<int>("NODECOUNT"!);
        }

        //first check if the user has voted before they can access the election
        [HttpGet]
        [Route("IsUserEligibleToVote/{election_id}")]
        public async Task<IActionResult> IsUserEligibleToVote([FromRoute] Guid election_id)
        {
            if (election_id == Guid.Empty)
            {
                return BadRequest("No valid election_id was provided.");
            }

            //get voter ID from claims
            Guid voter_id = await GetVoterIDFromClaims();

            try
            {
                bool eligible = await _voteService.IsUserEligibleToVote(voter_id, election_id);

                return Ok(new
                {
                    message = "Success, The user is allowed to vote in this election",
                    eligible = eligible
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

        //retrieve a token that can be used to cast a vote in a election
        [HttpGet]
        [Route("get_vote_token/{election_id}")]
        public async Task<IActionResult> GetVoteToken([FromRoute] Guid election_id) {
            if (election_id == Guid.Empty)
            {
                return BadRequest("No valid election_id was provided.");
            }

            try {
                //get voter ID from claims
                Guid voter_id = await GetVoterIDFromClaims();

                VoteTokenDto voteToken = await _voteService.CreateVoteToken(voter_id, election_id);

                if (voteToken == null) throw new Exception("Failed to create vote token");

                return Ok(new {
                    message = "Success",
                    token = voteToken
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


        //cast a vote towards an election
        [HttpPost]
        [Route("cast_vote")]
        public async Task<IActionResult> CastVote([FromBody] vote_request request) {

            try {
                //verify vote
                if (request == null) throw new Exception("Invalid request data");
                if (request.GetType() != typeof(vote_request)) throw new Exception("Invalid request format");

                //get voter ID from claims
                Guid voter_id = await GetVoterIDFromClaims();

                //check vote_request parameters - verify the token
                VotingToken token = await _voteService.VerifyVoteRequest(request);
                if (token == null) throw new Exception("Invalid voting token data");
                //save vote to database
                Vote vote = await _voteService.SaveVote(voter_id, request, token);
                if (vote == null) throw new Exception("Failed to save vote");

                //broadcast vote to other nodes
                VoteBroadcastRequest req = new VoteBroadcastRequest
                {
                    vote_id = vote.vote_id,
                    token_id = vote.token_id,
                    election_id = vote.election_id,
                    party_id = vote.party_id,
                    created_at = vote.created_at
                };

                transaction_response BroadCastSuccess = await _blockchainService.BroadCastVote(NODECOUNT, req);
                if (BroadCastSuccess == null) throw new Exception("Failed to broadcast vote");


                //generate vote receipt
                vote_receipt receipt = new vote_receipt
                {
                    election_id = vote.election_id,
                    hasVoted = true,
                    voted_at = vote.created_at
                };

                return Ok(new
                {
                    message = "Vote broadcasted successfully",
                    BroadCastSuccess = BroadCastSuccess,
                    receipt = receipt
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

        //check if the vote was placed
        [HttpGet]
        [Route("verify_vote/{vote_id}")]
        public async Task<IActionResult> VerifyVote_Receipt(Guid vote_id) {
            var vote_receipt = await _voteService.VerifyVote_Receipt(vote_id);

            if (vote_receipt == null) return NotFound("Vote receipt not found");

            return Ok(vote_receipt);
        }


        [HttpPost]
        [Route("RequestAccessToElection")]
        public async Task<IActionResult> RequestAccessToElection([FromBody] RequestElectionAccessDto request)
        {
            try
            {
                //get voter ID from claims
                Guid voter_id = await GetVoterIDFromClaims();

                var assignment = await _voteService.RequestAccessToElection(request.election_id, voter_id);

                return Ok(new
                {
                    message = "Voter successfully requested access to election.",
                    election_id = assignment.election_id,
                    voter_id = assignment.voter_id,
                    status = assignment.status,
                    has_voted = false,
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



        //get the voter's voter_id form jwt claims
        public async Task<Guid> GetVoterIDFromClaims()
        {
            var voterIdClaim = User.FindFirstValue(ClaimTypes.NameIdentifier);

            if (!Guid.TryParse(voterIdClaim, out Guid voter_id))
            {
                throw new Exception("Could not get voter_id form claims");
            }

            return voter_id;

        }




        [HttpGet]
        [Route("get_elections")]
        public async Task<IActionResult> GetElections([FromQuery] int limit = 10, [FromQuery] int page = 0)
        {
            try
            {
                List<Election> elecions = await _voteService.GetElections(limit, page);

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
        public async Task<IActionResult> GetParties([FromQuery] int limit = 10, [FromQuery] int page = 0)
        {
            try
            {
                List<Party> parties = await _voteService.GetParties(limit, page);

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


        [HttpGet("election_by_id/{election_id:guid}")]
        public async Task<IActionResult> GetElectionById([FromRoute] Guid election_id)
        {
            try
            {
                var election = await _voteService.GetElectionById(election_id);

                return Ok(new
                {
                    message = "Election information",
                    election = election
                });
            }
            catch (KeyNotFoundException ex)
            {
                return NotFound(new { message = ex.Message });
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error retrieving election: {ex}");

                return StatusCode(500, new
                {
                    message = "An unexpected error occurred while retrieving the election."
                });
            }
        }

        [HttpGet("party_by_id/{party_id:guid}")]
        public async Task<IActionResult> GetPartyById([FromRoute] Guid party_id)
        {
            try
            {
                var party = await _voteService.GetPartyById(party_id);

                return Ok(new
                {
                    message = "Party information",
                    party = party
                });
            }
            catch (KeyNotFoundException ex)
            {
                return NotFound(new { message = ex.Message });
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error retrieving party: {ex}");

                return StatusCode(500, new
                {
                    message = "An unexpected error occurred while retrieving the party."
                });
            }
        }


        [HttpGet]
        [Route("get_election_parties/{election_id:guid}")]
        public async Task<IActionResult> GetElectionParties([FromRoute] Guid election_id, [FromQuery] int limit = 10, [FromQuery] int page = 0)
        {
            try
            {
                var parties = await _voteService.GetElectionParties(election_id, limit, page);

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







    }
}
