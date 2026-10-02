using Microsoft.AspNetCore.Mvc;
using System.Text.Json;
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
        private readonly IConfiguration _configuration;

        private readonly int NODECOUNT = 0;

        public BlockchainController(VoteService voteService, BlockchainService blockchainService, IConfiguration configuration)
        {
            _voteService = voteService;
            _blockchainService = blockchainService;
            _configuration = configuration;
            NODECOUNT = _configuration.GetValue<int>("NODECOUNT"!);
        }

        //verifies and adds the vote to te current node's blockchain
        [HttpPost]
        [Route("handle_vote_request")]
        public async Task<IActionResult> HandleVoteRequest(vote_request request)//Fix check if this need [frombody]
        {
            try
            {
                //verify vote
                if (request == null) throw new Exception("Invalid request data");

                if (request.GetType() != typeof(vote_request)) throw new Exception("Invalid request format");

                //check valid voting token 
                VotingToken votingToken = await _voteService.VerifyVoteRequest(request);//should return voting token if valid, else return null

                if (votingToken == null) throw new Exception("Invalid voting token");

                //check if vote was submitted for this token <- if no vote was subitted it means the user did not vot yet
                //FIX we want to check if the user's vote is added to the database and if the token was used.
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
                    request.timestamp, //DateTime.UtcNow.ToUniversalTime(),//should be the timestamp of the vote
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
                return BadRequest($"handle_vote_request Error: {ex.Message}");
            }


        }

        //check each node's blockchain and see if they are valid
        [HttpGet]
        [Route("IsBlockchainValid")]
        public async Task<IActionResult> IsBlockchainValid()
        {
            try
            {
                var isBlockChainValid = await _blockchainService.IsBlockchainValid(NODECOUNT);

                if (isBlockChainValid == null) return NotFound("No blockchain data found");

                return Ok(new
                {
                    message = "Blockchain data",
                    isBlockChainValid = isBlockChainValid ? "All nodes' blockchains are valid" : "Some nodes' blockchains are invalid"
                });
            }
            catch (Exception ex)
            {
                return BadRequest($"IsBlockchainValid Error: {ex.Message}");
            }
        }

        //check the individual node's blockchain and see if it is valid
        [HttpGet]
        [Route("IsNodeChainValid")]
        public async Task<IActionResult> IsNodeChainValid()
        {
            try {
                var IsNodeChainValid = await _blockchainService.IsNodeChainValid();
                return Ok(IsNodeChainValid);
            }
            catch (Exception ex)
            {
                return BadRequest($"IsNodeChainValid Error: {ex.Message}");
            }
            
        }


        //get current nodes blockchain chain
        [HttpGet]
        [Route("GetNodeChain")]
        public async Task<IActionResult> GetNodeChain()
        {
            try
            {
                var chain = await _blockchainService.GetNodeChain();
                return Ok(chain);
            }
            catch (Exception ex)
            {
                return BadRequest($"GetNodeChain Error: {ex.Message}");
            }
        }


        //make a request to get the blockchain chain of a specific node by its id
        [HttpGet]
        [Route("GetNodeChain_id/{node_id}")]
        public async Task<IActionResult> GetNodeChain_id([FromRoute] int node_id)
        {
            try {
                if(node_id > NODECOUNT || node_id < 1)
                {
                    return BadRequest("Invalid node ID");
                }

                var chain = await _blockchainService.GetNodeChain_id(node_id);
                return Ok(chain);
            }
            catch (Exception ex)
            {
                return BadRequest($"GetNodeChain_id Error: {ex.Message}");
            }
            
        }
    }
}
