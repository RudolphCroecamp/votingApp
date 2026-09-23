using Microsoft.EntityFrameworkCore;
using votingBackend3.Data;
using votingBackend3.Models.DTOs;
using votingBackend3.Models.Entities;


namespace votingBackend3.Services
{
    public class BlockchainService
    {
        private readonly BlocksDbContext _blocksDbContext;
        private readonly HttpClient _httpClient;

        public BlockchainService(BlocksDbContext blocksDbContext, HttpClient httpClient)
        {
            _blocksDbContext = blocksDbContext;
            _httpClient = httpClient;
        }

        public async Task AddgenesisBlock()
        {
            var block_transaction = await _blocksDbContext.Database.BeginTransactionAsync();
            try
            {
                //check if a genesis block exist
                var hasBlocks = await _blocksDbContext.blocks.AnyAsync();
                if (hasBlocks) return;

                //create genesis block
                Block genesisBlock = new Block(0, DateTime.UtcNow, "genesis block", "");

                //add to database
                _blocksDbContext.blocks.Add(genesisBlock);

                //save changes
                await _blocksDbContext.SaveChangesAsync();

                //commit changes
                await block_transaction.CommitAsync();
            }
            catch (Exception ex)
            {
                await block_transaction.RollbackAsync();
                throw new Exception(ex.Message);
            }
        }

        public async Task<Block> GetLatestBlock()
        {
            try
            {
                return await _blocksDbContext.blocks.OrderByDescending(b => b.index).FirstAsync();
            }
            catch (Exception ex) {
                throw new Exception(ex.Message);
            }
        }

        public async Task addBlock(Block newBlock)
        {
            var block_transaction = await _blocksDbContext.Database.BeginTransactionAsync();
            try
            {
                newBlock.previousHash = (await GetLatestBlock()).hash;
                //add to database
                _blocksDbContext.blocks.Add(newBlock);

                //save changes
                await _blocksDbContext.SaveChangesAsync();

                //commit changes
                await block_transaction.CommitAsync();
            }
            catch (Exception ex)
            {
                await block_transaction.RollbackAsync();
                throw new Exception(ex.Message);
            }
        }


        public async Task<transaction_response> BroadCastVote(int numNodes, vote_request request )
        { //return true if all nodes received a transaction, false if any node failed to receive it --fix later

            //store the responses from nodes
            transaction_response transaction_response = new transaction_response
            {
                messages = new List<string>(),
                status = new List<bool>()
            };


            for (int i = 1; i <= numNodes; i++)
            {
                try
                {
                    using HttpResponseMessage response = await _httpClient.PostAsJsonAsync($"http://node{i}:8080/api/Blockchain/handle_vote_request", request);//node{i} represents the internal docker node
                    response.EnsureSuccessStatusCode();
                    string responseBody = await response.Content.ReadAsStringAsync();

                    transaction_response.messages.Add(responseBody);
                    transaction_response.status.Add(true);      
                }
                catch (HttpRequestException e)
                {
                    transaction_response.messages.Add($"Node {i}: {e.Message}");
                    transaction_response.status.Add(false);
                }
            }

            return transaction_response;
        }

    }

}
