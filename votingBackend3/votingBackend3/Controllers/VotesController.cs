using Microsoft.AspNetCore.Mvc;
using System.Security.Cryptography.X509Certificates;
using votingBackend3.Models.DTOs;
using votingBackend3.Models.Entities;
using votingBackend3.Services;

namespace votingBackend3.Controllers
{
    //[Authorize(Roles = "Voter")]
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

        //FIX block the add to blockchain request if vote token was already used
        //FIX block the token after it was used
        //TODO add authentication and authorisation to broad_cast_vote -> pass user's jwt

        [HttpPost]
        [Route("get_vote_token")]
        public async Task<IActionResult> GetVoteToken(VoteTokenRequest VoteTokenRequest) {
            try { 
                VoteToken voteToken = await _voteService.CreateVoteToken(VoteTokenRequest);

                if (voteToken == null) throw new Exception("Failed to create vote token" );

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

        [HttpPost]
        [Route("cast_vote")]
        public async Task<IActionResult> CastVote([FromBody] vote_request request) {

            try {
                //verify vote
                if (request == null) throw new Exception("Invalid request data");
                if (request.GetType() != typeof(vote_request)) throw new Exception("Invalid request format");

                //check vote_request parameters
                VotingToken token = await _voteService.VerifyVoteRequest(request);

                if (token == null) throw new Exception("Invalid voting token data");

                //save vote to database
                vote_response vote_receipt = await _voteService.SaveVote(request, token);

                //broadcast vote to other nodes
                transaction_response broadcastResult = await _blockchainService.BroadCastVote(NODECOUNT, request);

                if (broadcastResult == null) throw new Exception("Failed to broadcast vote");//FIX

                return Ok(new
                {
                    message = "Vote broadcasted successfully",
                    data = broadcastResult,
                    receipt = vote_receipt
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
        [Route("verify_vote/{vote_id}")]
        public async Task<IActionResult> VerifyVote_Receipt(Guid vote_id) { 
            var vote_receipt = await _voteService.VerifyVote_Receipt(vote_id);

            if (vote_receipt == null) return NotFound(new { message = "Vote receipt not found" });

            return Ok(vote_receipt);
        }

    }
}
