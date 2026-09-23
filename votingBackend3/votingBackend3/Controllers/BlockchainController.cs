using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using System.Text.Json;
using System.Transactions;
using votingBackend3.Data;
using votingBackend3.Models.DTOs;
using votingBackend3.Models.Entities;
using votingBackend3.Services;

namespace votingBackend3.Controllers
{
    //[Authorize(Roles = "Voter")]
    [Route("api/[controller]")]
    [ApiController]
    public class BlockchainController : ControllerBase
    {
        private readonly VoteService _voteService;
        private readonly BlockchainService _blockchainService;

        public BlockchainController(VoteService voteService, BlockchainService blockchainService)
        {
            _voteService = voteService;
            _blockchainService = blockchainService;
        }


        [HttpPost]
        [Route("handle_vote_request")]
        public async Task<IActionResult> HandleVoteRequest(vote_request request) {
            try
            {
                //verify vote
                if (request == null) throw new Exception("Invalid request data");

                if (request.GetType() != typeof(vote_request)) throw new Exception("Invalid request format");

                //check valid voting token 
                VotingToken votingToken = await _voteService.VerifyVoteRequest(request);//should return voting token if valid, else return null

                if (votingToken == null) throw new Exception("Invalid voting token" );

                //check if vote was submitted for this token
                Vote vote = await _voteService.VerifyVote(request);

                if (vote == null) throw new Exception("No vote was found for this token");

                BlockData newBlockData = new BlockData
                {
                    vote_id = vote.vote_id,
                    token_id = vote.token_id,
                    election_id = vote.election_id,
                    party_id = vote.party_id,
                };

                string blockdataJson = JsonSerializer.Serialize(newBlockData);


                Block previousBlock = await _blockchainService.GetLatestBlock();

                Block newBlock = new Block(
                    previousBlock.index + 1,
                    DateTime.UtcNow,
                    blockdataJson,
                    previousBlock.hash
                );


                await _blockchainService.addBlock(newBlock);

                return Ok(new
                {
                    message = "Block added successfully",
                    data = newBlock,
                });


            }
            catch (Exception ex) {
                Console.WriteLine(ex.Message);
                return BadRequest($"Blockchain Error: {ex.Message}");
            }
            

        }

    }
}
