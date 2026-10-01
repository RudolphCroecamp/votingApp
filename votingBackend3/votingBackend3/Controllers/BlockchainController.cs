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
    [Route("api/[controller]")]
    [ApiController]
    public class BlockchainController : ControllerBase
    {
        private readonly VoteService _voteService;
        private readonly BlockchainService _blockchainService;

        private readonly int NODECOUNT = 4;

        public BlockchainController(VoteService voteService, BlockchainService blockchainService)
        {
            _voteService = voteService;
            _blockchainService = blockchainService;
        }


        [HttpPost]
        [Route("handle_vote_request")]
        public async Task<IActionResult> HandleVoteRequest(vote_request request)
        {
            try
            {
                //verify vote
                if (request == null) throw new Exception("Invalid request data");

                if (request.GetType() != typeof(vote_request)) throw new Exception("Invalid request format");

                //check valid voting token 
                VotingToken votingToken = await _voteService.VerifyVoteRequest(request);//should return voting token if valid, else return null

                if (votingToken == null) throw new Exception("Invalid voting token");

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
                    DateTime.UtcNow.ToUniversalTime(),
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
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                return BadRequest($"Blockchain Error: {ex.Message}");
            }


        }


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
        [Route("IsNodeChainValid")]
        public async Task<IActionResult> IsNodeChainValid()
        {
            var IsNodeChainValid = await _blockchainService.IsNodeChainValid();
            return Ok(IsNodeChainValid);
        }


        //[HttpGet]
        //[Route("GetNodeChain_id/{nodeId}")]
        //public async Task<IActionResult> GetNodeChain_id(int nodeId)
        //{
        //    var chain = await _blockchainService.GetNodeChain_id(nodeId);
        //    return Ok(chain);
        //}

        //[HttpGet]
        //[Route("GetNodeChain")]
        //public async Task<IActionResult> GetNodeChain()
        //{
        //    var chain = await _blockchainService.GetNodeChain();
        //    return Ok(chain);
        //}

    }
}
