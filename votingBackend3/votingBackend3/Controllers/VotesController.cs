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


        //get the voter's voter_id form jwt claims
        public async Task<Guid> GetVoterIDFromClaims(){
            var voterIdClaim = User.FindFirstValue(ClaimTypes.NameIdentifier);

            if (!Guid.TryParse(voterIdClaim, out Guid voter_id))
            {
                return Guid.Empty;
            }

            return voter_id;

        }

    }
}
