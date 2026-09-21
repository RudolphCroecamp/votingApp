using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using System.Runtime.CompilerServices;
using votingBackend3.Models.DTOs;
using votingBackend3.Models.Entities;
using votingBackend3.Services;

namespace votingBackend3.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class VotesController : ControllerBase
    {
        private readonly VoteService _voteService;
        private readonly BlockchainService _blockchainService;

        public VotesController(VoteService voteService, BlockchainService blockchainService)
        {
            _voteService = voteService;
            _blockchainService = blockchainService;
        }




        [HttpPost]
        [Route("get_vote_token")]
        public async Task<IActionResult> GetVoteToken(VoteTokenRequest VoteTokenRequest) {
            VoteToken voteToken = await _voteService.CreateVoteToken(VoteTokenRequest);

            if (voteToken == null) return BadRequest(new { error = "Failed to create vote token" });

            return Ok(new { 
                message = "Success",
                token = voteToken
            });
        }


        [HttpPost]
        [Route("cast_vote")]
        public async Task<IActionResult> CastVote([FromBody] vote_request request) {

            //verify vote

            if (request == null) return BadRequest(new { error = "Invalid request data" });

            if(request.GetType() != typeof(vote_request)) return BadRequest(new { error = "Invalid request format" });

            //check vote_request parameters
            VotingToken token = await _voteService.VerifyVoteRequest(request);

            if (token == null) return BadRequest(new { error = "Invalid voting token data" });

            //save vote to database
            vote_response vote_receipt = await _voteService.SaveVote(request, token);

            //broadcast vote to other nodes
            transaction_response broadcastResult = await _blockchainService.BroadCastVote(4, request);

            if (broadcastResult == null) return BadRequest(new { error = "Failed to broadcast vote" });//fix later

            

            return Ok(new {
                message = "Vote broadcasted successfully",
                data = broadcastResult,
                receipt = vote_receipt
            });
        }

    }
}
