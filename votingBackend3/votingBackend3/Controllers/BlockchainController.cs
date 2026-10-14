using Microsoft.AspNetCore.Authorization;
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
        [Authorize(Policy = "NodeVoteReceiver")]
        public async Task<IActionResult> HandleVoteRequest(VoteBroadcastRequest request)
        {
            try
            {
                //verify vote
                if (request == null) throw new Exception("Invalid request data");

                if (request.GetType() != typeof(VoteBroadcastRequest)) throw new Exception("Invalid request format");

                //create block data from the vote information
                BlockData newBlockData = new BlockData
                {
                    vote_id = request.vote_id,
                    token_id = request.token_id,
                    election_id = request.election_id,
                    party_id = request.party_id
                };
                //deserialise vote information to json string to be stored in the block
                string blockdataJson = JsonSerializer.Serialize(newBlockData);
                //get the latest block form the blockchain to create a new block with the previous block's hash
                Block previousBlock = await _blockchainService.GetLatestBlock();
                //create block
                Block newBlock = new Block(
                    previousBlock.index + 1,
                    request.created_at,
                    blockdataJson,
                    previousBlock.hash
                );

                //validate the voteing data
                bool isValidVote = await _voteService.ValidateBroadCastRequest(request, newBlock.hash);
                if (isValidVote != true) throw new Exception("Could not validate vote data");
                
                //save the block to blockchain
                await _blockchainService.addBlock(newBlock);

                return Ok(new
                {
                    message = "Block added successfully",
                    data = newBlock,
                });


            }
            catch (Exception ex)
            {
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



        //check node health
        [HttpGet]
        [Route("GetNodeHealth/{node_id}")]
        public async Task<IActionResult> GetNodeHealth([FromRoute] int node_id)
        {
            try
            {
                if (node_id > NODECOUNT || node_id < 1)
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
